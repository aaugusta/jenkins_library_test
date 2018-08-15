import groovy.json.JsonSlurperClassic

def call(init_token) {
	/*
		vaultToken	: passed in by user, used to retrieve secret ID
		libToken: 	: used to retrieve Role ID of current project 
		vault_addr 	: address of vault server for http api requests
	*/

	String vaultToken = init_token
	String libToken = "a422adb9-abce-e295-e81b-3ed4e6408186"
	String vault_addr = 'http://127.0.0.1:8200'


	//looks up Token information so we can extract the policy
	sh(script: """
		curl --header "X-Vault-Token: $vaultToken" \
			'$vault_addr'/v1/auth/token/lookup-self -o policy.JSON 
	""", returnStdout: true)

	def policies = parseJSON("policy.JSON").data.policies
	String policy = "default"
	for(int i = 0; i < policies.size(); i++) {
		if(!policies[i].equals("default")) {
			policy = policies[i]
		}	
	}
	/*
		policies for user tokens follow naming convention of ${projectName}-id,
		so we remove the last 3 characters to retrieve the project name
	*/
	String project = policy.substring(0, policy.size()-3)

	//retrieve Role ID using project name 
	sh(script: """
		curl --header "X-Vault-Token: $libToken" \
			'$vault_addr'/v1/secret/roles/'$project' -o role.JSON
	""", returnStdout: true)
	String roleID = parseJSON("role.JSON").data.roleID



	//retrieves secret_id for approle authentication
	sh(script: """
		set +x
		curl --header "X-Vault-Token: $vaultToken" \
			 --request POST '$vault_addr'/v1/auth/approle/role/'$project'-role/secret-id \
			 -o secretID.JSON
	""", returnStdout: true)
	String secretID = parseJSON("secretID.JSON").data.secret_id


	//retrieves token to access secrets associated with given role
	String payload = /{"role_id": "'$roleID'", "secret_id": "'$secretID'"}/
	sh(script: """
		set +x
		curl --request POST --data '$payload' '$vault_addr'/v1/auth/approle/login \
		-o secretToken.JSON
	""", returnStdout: true)
	String secretToken = parseJSON("secretToken.JSON").auth.client_token


	//retrieve secrets 
	sh(script: """
		set +x		
		curl --header "X-Vault-Token: $secretToken" \
		'$vault_addr'/v1/secret/'$project'/creds -o creds.JSON

	""", returnStdout: true)
	
	def creds = parseJSON("creds.JSON")
	String subID = creds.data.subID
	String clientID = creds.data.clientID
	String clientSecret = creds.data.clientSecret
	String tenantID = creds.data.tenantID


	sh(script: """
		set +x
		rm creds.JSON
		rm secretID.JSON
		rm secretToken.JSON
		rm role.JSON
		rm policy.JSON
	""", returnStdout: true)



	String terraVars = """azure_subscription_id = "$subID",
	azure_client_id = "$clientID",
	azure_client_secret = "$clientSecret",
	azure_tenant_id = "$tenantID",
	ssh_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDwGVt2Zhiv+XvARIH7vuzOf5M1ztP0VkS07RIiGb2asM9f7nPj7tH0iy1i1nB5GII0+AMwMx90B4EhUXZsOWkmnZFzVWto2+6f5orz3Pl4axE9+7OeO0xOoyDiIrkDUVor02zzYY+dP8I5AGHLvG26gzz5tt5toWixtilJ4ZmXFkQwHB7ghOG1mhOrVxGbAqW44TC3IA1Ogh7zDycqsz2af2GDT5Sd0nFDciCJiH5ax7n9XtlY9zGfd6sfHFw3a62q5+mnTM4owGyWW7YTd3fQJ/2qR6XnPDKS/YfqtXfpX1q6os3i+LgoCZ2CRKyAD1xcooHQiTtu8+Y3HJeXWDDD james@james-VirtualBox"
	"""

	//populate secrets file -> apply terraform plan -> deploy cluster
	writeFile file: "k8s.tfvars", text: "$terraVars"


	sh """
		terraform state rm ""
		terraform init
		terraform apply -auto-approve -var-file=k8s.tfvars
	"""
	// azureCLI commands: [[exportVariablesString: '', script: 'az group deployment create --name k8s-cluster --resource-group kubegroup --template-file var/lib/jenkins/workspace/pipeline_demo_master-YCLVMIFKQWOHG4NMQXMJVJZU3W6QMPWGKPDBHFPXCCLCPYAAV4UQ/_output/kubegroup-k8s-cluster/azuredeploy.json --parameters var/lib/jenkins/workspace/pipeline_demo_master-YCLVMIFKQWOHG4NMQXMJVJZU3W6QMPWGKPDBHFPXCCLCPYAAV4UQ/_output/kubegroup-k8s-cluster/azuredeploy.parameters.json']], principalCredentialId: 'kubegroup_sp'
	// sh "rm k8s.tfvars"

	
	
}


def parseJSON(file) {
	try{
		def tokenInfo = sh(script: "cat $file", returnStdout: true)
		def jsonSlurper = new JsonSlurperClassic()
		info = jsonSlurper.parseText(tokenInfo)
		return info

	}
	catch(Exception e) {
		println(e.getMessage())
	}
}