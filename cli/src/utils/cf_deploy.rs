use std::error::Error;
use std::time::Duration;

use aws_sdk_cloudformation::{
    Client,
    types::{Capability, Parameter},
};
use aws_sdk_cloudformation::client::Waiters;

const TIMEOUT: Duration = Duration::from_secs(1800);

pub struct CfDeploy {
    client: Client,
    parameters: Option<Vec<Parameter>>,
    template: String,
    stack: String,
    capabilities: Option<Vec<Capability>>,
}

impl CfDeploy {
    pub async fn connect(
        profile: &str,
        stack: &str,
        file_path: &str,
    ) -> Result<Self, Box<dyn Error>> {
        let config = aws_config::from_env().profile_name(profile).load().await;
        let client = Client::new(&config);
        let template = std::fs::read_to_string(file_path)?;
        Ok(Self {
            client,
            parameters: None,
            capabilities: None,
            template,
            stack: stack.to_string(),
        })
    }

    pub fn parameter(&mut self, key: &str, value: &str) -> &mut Self {
        if self.parameters.is_none() {
            self.parameters = Some(Vec::new())
        }
        self.parameters.as_mut().unwrap().push(
            Parameter::builder()
                .parameter_key(key)
                .parameter_value(value)
                .build(),
        );
        self
    }

    pub fn capability(&mut self, name: &str) -> Result<&mut Self, Box<dyn Error>> {
        let capability = Capability::try_parse(name)?;
        if self.capabilities.is_none() {
            self.capabilities = Some(Vec::new())
        }
        self.capabilities.as_mut().unwrap().push(capability);
        Ok(self)
    }

    pub async fn deploy(&self) -> Result<(), Box<dyn Error>> {
        let exists = self
            .client
            .describe_stacks()
            .stack_name(self.stack.clone())
            .send()
            .await;
        match exists {
            Ok(_) => {
                self.client
                    .update_stack()
                    .stack_name(self.stack.clone())
                    .template_body(self.template.clone())
                    .set_parameters(self.parameters.clone())
                    .set_capabilities(self.capabilities.clone())
                    .send()
                    .await?;
                self.client
                    .wait_until_stack_update_complete()
                    .stack_name(self.stack.clone())
                    .wait(TIMEOUT)
                    .await?;
                Ok(())
            }
            Err(_) => {
                self.client
                    .create_stack()
                    .stack_name(self.stack.clone())
                    .template_body(self.template.clone())
                    .set_parameters(self.parameters.clone())
                    .set_capabilities(self.capabilities.clone())
                    .send()
                    .await?;
                self.client
                    .wait_until_stack_create_complete()
                    .stack_name(self.stack.clone())
                    .wait(TIMEOUT)
                    .await?;
                Ok(())
            }
        }
    }

    pub async fn destroy(&self) -> Result<(), Box<dyn Error>> {
        self.client
            .delete_stack()
            .stack_name(self.stack.clone())
            .send()
            .await?;
        self.client
            .wait_until_stack_delete_complete()
            .stack_name(self.stack.clone())
            .wait(TIMEOUT)
            .await?;
        Ok(())
    }
}
