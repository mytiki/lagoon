use std::error::Error;
use std::io::Write;
use std::process::{Command, Stdio};

pub struct Docker {
    uri: String,
    repository: String,
    context: Option<String>,
    tag: Option<String>,
    region: String,
    account: String,
    target: Option<String>,
    platform: Option<String>,
}

impl Docker {
    pub fn new(account: &str, region: &str, repository: &str) -> Self {
        Docker {
            uri: format!("{}.dkr.ecr.{}.amazonaws.com", account, region),
            repository: repository.to_string(),
            context: None,
            tag: None,
            region: region.to_string(),
            account: account.to_string(),
            target: None,
            platform: None,
        }
    }

    pub fn with_context(mut self, context: &str) -> Self {
        self.context = Some(context.to_string());
        self
    }

    pub fn with_tag(mut self, tag: &str) -> Self {
        self.tag = Some(tag.to_string());
        self
    }

    pub fn with_target(mut self, target: &str) -> Self {
        self.target = Some(target.to_string());
        self
    }

    pub fn with_platform(mut self, platform: &str) -> Self {
        self.platform = Some(platform.to_string());
        self
    }

    pub fn create_repository(account: &str, region: &str) -> String {
        format!("{}.dkr.ecr.{}.amazonaws.com", account, region)
    }

    pub fn auth(self, profile: &str) -> Result<Self, Box<dyn Error>> {
        let aws_output = Command::new("aws")
            .arg("ecr")
            .arg("get-login-password")
            .arg("--region")
            .arg(self.region.clone())
            .arg("--profile")
            .arg(profile)
            .output()?;
        if !aws_output.status.success() {
            Err(format!("{} auth failed: {:?}", self.repository, aws_output))?;
        }
        let mut docker_command = Command::new("docker")
            .arg("login")
            .arg("--username")
            .arg("AWS")
            .arg("--password-stdin")
            .arg(self.uri.clone())
            .stdin(Stdio::piped())
            .spawn()?;
        docker_command
            .stdin
            .as_mut()
            .unwrap()
            .write_all(&aws_output.stdout)?;
        let docker_output = docker_command.wait_with_output()?;
        if !docker_output.status.success() {
            Err(format!(
                "{} auth failed: {:?}",
                self.repository, docker_output
            ))?;
        }
        Ok(self)
    }

    pub fn build(self) -> Result<Self, Box<dyn Error>> {
        let docker_tag = format!(
            "{}/{}:{}",
            self.uri,
            self.repository,
            self.tag.clone().unwrap_or("latest".to_string())
        );
        let mut command = Command::new("docker");
        command
            .arg("build")
            .arg("--tag")
            .arg(&docker_tag)
            .arg(&self.context.clone().unwrap_or(".".to_string()));
        if let Some(target) = self.target.clone() {
            command.arg("--target").arg(target);
        }
        if let Some(platform) = self.platform.clone() {
            command.arg("--platform").arg(platform);
        }
        let res = command.output()?;
        if !res.status.success() {
            Err(format!("{} build failed: {:?}", self.repository, res))?;
        }
        Ok(self)
    }

    pub fn push(&self) -> Result<(), Box<dyn Error>> {
        let docker_tag = format!(
            "{}/{}:{}",
            self.uri,
            self.repository,
            self.tag.clone().unwrap_or("latest".to_string())
        );
        let res = Command::new("docker")
            .arg("push")
            .arg(&docker_tag)
            .output()?;
        if res.status.success() {
            Ok(())
        } else {
            Err(format!("{} push failed: {:?}", self.repository, res).into())
        }
    }

    pub fn repository(&self) -> &str {
        &self.repository
    }
    pub fn context(&self) -> &Option<String> {
        &self.context
    }
    pub fn tag(&self) -> &Option<String> {
        &self.tag
    }
    pub fn region(&self) -> &str {
        &self.region
    }
    pub fn account(&self) -> &str {
        &self.account
    }
    pub fn target(&self) -> &Option<String> {
        &self.target
    }
}
