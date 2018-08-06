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

	sh """
		ls
		echo sub: '$subID'
		echo cID: '$clientID'
		echo cS: '$clientSecret'
		echo ten: '$tenantID'
		echo dns: '$dnsPrefix'
	"""

	String terraVars = """azure_subcription_id = "$subID",
	azure_client_id = "$clientID"
	azure_client_secret = "$clientSecret"
	azure_tenant_id = "$tenantID"
	dns_prefix = "$dnsPrefix"
	ssh_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDwGVt2Zhiv+XvARIH7vuzOf5M1ztP0VkS07RIiGb2asM9f7nPj7tH0iy1i1nB5GII0+AMwMx90B4EhUXZsOWkmnZFzVWto2+6f5orz3Pl4axE9+7OeO0xOoyDiIrkDUVor02zzYY+dP8I5AGHLvG26gzz5tt5toWixtilJ4ZmXFkQwHB7ghOG1mhOrVxGbAqW44TC3IA1Ogh7zDycqsz2af2GDT5Sd0nFDciCJiH5ax7n9XtlY9zGfd6sfHFw3a62q5+mnTM4owGyWW7YTd3fQJ/2qR6XnPDKS/YfqtXfpX1q6os3i+LgoCZ2CRKyAD1xcooHQiTtu8+Y3HJeXWDDD james@james-VirtualBox"
	"""

	sh """
		rm k8s.tfvars
		rm secretID.JSON
		rm secretToken.JSON
		rm sub.JSON
		rm tenant.JSON
	"""

	writeFile("k8s.tfvars", "$terraVars")
	sh "ls; cat k8s.tfvars"
	// String output = sh(script: """
	
	// 	export VAULT_ADDR=$vault_addr
	// 	vault login $secretToken > /dev/null
	// 	vault kv get -field=id my-secret/data/subID
	// 	vault kv get -field=id my-secret/data/clientID
	// 	vault kv get -field=id my-secret/data/clientSecret
	// 	vault kv get -field=id my-secret/data/tenantID  
	// """, returnStdout: true)

	// sh """
	// 	export VAULT_ADDR=$vault_addr
	// 	echo $output
	// """


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