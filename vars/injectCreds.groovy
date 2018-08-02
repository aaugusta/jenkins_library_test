

def call(init_token){
	String vaultToken = init_token
	String roleID = "18541985-b756-d102-1e4c-e35695d9f647"
	String vault_addr = 'http://127.0.0.1:8200'


	String secretID = sh(script: """
		set +x
		export VAULT_ADDR=$vault_addr
		vault login $vaultToken > /dev/null
		vault write -field=secret_id -f 'auth/role/jenkins-azure/secret_id' 
	""", returnStdout: true)

	String secretToken = sh(script: """
		set +x
		export VAULT_ADDR=$vault_addr
		vault write -field=token auth/approle/login role_id=$roleID secret_id=$secretID
	""", returnStdout: true)

	String output = sh(script: """
		set +x
		export VAULT_ADDR=$vault_addr
		vault login $secretToken > /dev/null
		vault kv get -field=id my-secret/data/subID
		vault kv get -field=id my-secret/data/clientID
		vault kv get -field=id my-secret/data/clientSecret
		vault kv get -field=id my-secret/data/tenantID  
	""", returnStdout: true)

	sh """
		export VAULT_ADDR=$vault_addr
		echo $output
	"""
}