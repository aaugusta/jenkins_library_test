import groovy.json.JsonSlurperClassic

def call(init_token) {
	String vaultToken = init_token
	String roleID = '20c5906d-6106-696e-9288-7e274df11f13'
	String vault_addr = 'http://127.0.0.1:8200'

	//retrieves secret_id for approle authentication
	sh(script: """
		set +x
		curl --header "X-Vault-Token: $vaultToken" \
			 --request POST '$vault_addr'/v1/auth/approle/role/jenkins-azure/secret-id \
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
		'$vault_addr'/v1/my-secret/data/subID -o subID.JSON

		curl --header "X-Vault-Token: $secretToken" \
		'$vault_addr'/v1/my-secret/data/clientID -o clientID.JSON

		curl --header "X-Vault-Token: $secretToken" \
		'$vault_addr'/v1/my-secret/data/clientSecret -o clientSecret.JSON

		curl --header "X-Vault-Token: $secretToken" \
		'$vault_addr'/v1/my-secret/data/tenantID -o tenantID.JSON

		curl --header "X-Vault-Token: $secretToken" \
		'$vault_addr'/v1/my-secret/data/dnsPrefix -o dnsPrefix.JSON
	""", returnStdout: true)
	
	String subID = parseJSON("subID.JSON").data.id
	String clientID = parseJSON("clientID.JSON").data.id
	String clientSecret = parseJSON("clientSecret.JSON").data.id
	String tenantID = parseJSON("tenantID.JSON").data.id
	String dnsPrefix = parseJSON("dnsPrefix.JSON").data.id

	sh(script: """
		set +x
		rm subID.JSON
		rm clientID.JSON
		rm clientSecret.JSON
		rm tenantID.JSON
		rm dnsPrefix.JSON
	""", returnStdout: true)



	String terraVars = """azure_subcription_id = "$subID",
	azure_client_id = "$clientID"
	azure_client_secret = "$clientSecret"
	azure_tenant_id = "$tenantID"
	dns_prefix = "$dnsPrefix"
	ssh_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDwGVt2Zhiv+XvARIH7vuzOf5M1ztP0VkS07RIiGb2asM9f7nPj7tH0iy1i1nB5GII0+AMwMx90B4EhUXZsOWkmnZFzVWto2+6f5orz3Pl4axE9+7OeO0xOoyDiIrkDUVor02zzYY+dP8I5AGHLvG26gzz5tt5toWixtilJ4ZmXFkQwHB7ghOG1mhOrVxGbAqW44TC3IA1Ogh7zDycqsz2af2GDT5Sd0nFDciCJiH5ax7n9XtlY9zGfd6sfHFw3a62q5+mnTM4owGyWW7YTd3fQJ/2qR6XnPDKS/YfqtXfpX1q6os3i+LgoCZ2CRKyAD1xcooHQiTtu8+Y3HJeXWDDD james@james-VirtualBox"
	"""

	//populate secrets file -> apply terraform plan -> deploy cluster
	writeFile file: "k8s.tfvars", text: "$terraVars"
	sh """
		terraform state rm ""
		terraform init
		terraform apply -auto-approve -var-file=k8s.tfvars
	"""
	azureCLI commands: [[exportVariablesString: '', script: 'az group deployment create --name k8s-cluster --resource-group kubegroup --template-file var/lib/jenkins/workspace/pipeline_demo_master-YCLVMIFKQWOHG4NMQXMJVJZU3W6QMPWGKPDBHFPXCCLCPYAAV4UQ/_output/kubegroup-k8s-cluster/azuredeploy.json --parameters var/lib/jenkins/workspace/pipeline_demo_master-YCLVMIFKQWOHG4NMQXMJVJZU3W6QMPWGKPDBHFPXCCLCPYAAV4UQ/_output/kubegroup-k8s-cluster/azuredeploy.parameters.json']], principalCredentialId: 'kubegroup_sp'



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