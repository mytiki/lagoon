use std::{error::Error, fs::File, io::prelude::*, path::Path};

use walkdir::{DirEntry, WalkDir};
use zip::{result::ZipError, write::SimpleFileOptions};

pub fn new(
    src_dir: &str,
    dst_file: &str,
    method: zip::CompressionMethod,
) -> Result<(), Box<dyn Error>> {
    let src_path = Path::new(src_dir);
    let dst_path = Path::new(dst_file);
    if !src_path.is_dir() {
        return Err(ZipError::FileNotFound.into());
    }
    let dest_file = File::create(dst_path)?;
    let walkdir = WalkDir::new(src_dir);
    let it = walkdir.into_iter();
    zip_items(&mut it.filter_map(|e| e.ok()), src_path, dest_file, method)?;
    Ok(())
}

fn zip_items<T>(
    it: &mut dyn Iterator<Item = DirEntry>,
    prefix: &Path,
    writer: T,
    method: zip::CompressionMethod,
) -> Result<(), Box<dyn Error>>
where
    T: Write + Seek,
{
    let mut zip = zip::ZipWriter::new(writer);
    let options = SimpleFileOptions::default()
        .compression_method(method)
        .unix_permissions(0o755);
    let prefix = Path::new(prefix);
    let mut buffer = Vec::new();
    for entry in it {
        let path = entry.path();
        let name = path.strip_prefix(prefix).unwrap();
        let path_as_string = name
            .to_str()
            .map(str::to_owned)
            .ok_or(format!("{name:?} Is a Non UTF-8 Path"))?;
        if path.is_file() {
            log::debug!("adding file {path:?} as {name:?} ...");
            zip.start_file(path_as_string, options)?;
            let mut f = File::open(path)?;
            f.read_to_end(&mut buffer)?;
            zip.write_all(&buffer)?;
            buffer.clear();
        } else if !name.as_os_str().is_empty() {
            log::debug!("adding dir {path_as_string:?} as {name:?} ...");
            zip.add_directory(path_as_string, options)?;
        }
    }
    zip.finish()?;
    Ok(())
}
