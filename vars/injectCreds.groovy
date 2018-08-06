import groovy.json.JsonSlurperClassic

def call(init_token) {
	String vaultToken = init_token
	String roleID = "20c5906d-6106-696e-9288-7e274df11f13"
	String vault_addr = 'http://127.0.0.1:8200'


	sh(script: """
		export VAULT_ADDR=$vault_addr
		curl --header "X-Vault-Token: $vaultToken" \
			 --request POST '$vault_addr'/v1/auth/approle/role/jenkins-azure/secret-id \
			 -o secretID.json
		cat secretID.JSON
	""")

	def info
	String secretID
	try {
		def tokenInfo = sh(script: "cat secretID.JSON", returnStdout: true)
		def jsonSlurper = new JsonSlurperClassic()	
		info = jsonSlurper.parseText(tokenInfo)
		secretID = info.data.secret_id
	}
	catch(Exception e) {
		sh 'echo dang it failed'
		println(e.getMessage())
	}

	sh """
		echo $secretID"
		echo '$secretID'
		echo hello
	"""
	// String secretToken = sh(script: """

	// 	export VAULT_ADDR=$vault_addr
	// 	vault write -field=token auth/approle/login role_id=$roleID secret_id=$secretID
	// """, returnStdout: true)

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