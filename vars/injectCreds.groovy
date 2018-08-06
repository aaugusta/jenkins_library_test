import groovy.json.JsonSlurperClassic

def call(init_token) {
	String vaultToken = init_token
	String roleID = '20c5906d-6106-696e-9288-7e274df11f13'
	String vault_addr = 'http://127.0.0.1:8200'

	//retrieves secret_id for approle authentication
	sh(script: """
		curl --header "X-Vault-Token: $vaultToken" \
			 --request POST '$vault_addr'/v1/auth/approle/role/jenkins-azure/secret-id \
			 -o secretID.JSON
	""", returnStdout: true)
	String secretID = parseJSON("secretID.JSON").data.secret_id


	//retrieves token to access secrets associated with given role
	String payload = /{"role_id": "'$roleID'", "secret_id": "'$secretID'"}/
	sh(script: """
		curl --request POST --data '$payload' '$vault_addr'/v1/auth/approle/login \
		-o secretToken.JSON
	""")
	String secretToken = parseJSON("secretToken.JSON").auth.client_token


	//retrieve secrets 
	sh """
		curl --header "X-Vault-Token: $secretToken" \
		'$vault_addr'/v1/my-secret/data/subID -o subID.JSON

		curl --header "X-Vault-Token: $secretToken" \
		'$vault_addr'/v1/my-secret/data/clientID -o clientID.JSON

		curl --header "X-Vault-Token: $secretToken" \
		'$vault_addr'/v1/my-secret/data/clientSecret -o clientSecret.JSON

		curl --header "X-Vault-Token: $secretToken" \
		'$vault_addr'/v1/my-secret/data/tenantID -o tenant.JSON

		curl --header "X-Vault-Token: $secretToken" \
		'$vault_addr'/v1/my-secret/data/dnsPrefix -o dnsPrefix.JSON
	"""
	String subID = parseJSON("subID.JSON").data.id
	String clientID = parseJSON("clientID.JSON").data.id
	String clientSecret = parseJSON("clientSecret.JSON").data.id
	String tenantID = parseJSON("tenantID.JSON").data.id
	String dnsPrefix = parseJSON("dnsPrefix.JSON").data.id

	sh """
		rm subID.JSON
		rm clientID.JSON
		rm clientSecret.JSON
		rm tenantID.JSON
		rm dnsPrefix.JSON
	"""

	sh """
		ls
		echo sub: '$subID'
		echo cID: '$clientID'
		echo cS: '$clientSecret'
		echo ten: '$tenantID'
		echo dns: 'dnsPrefix'
	"""

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