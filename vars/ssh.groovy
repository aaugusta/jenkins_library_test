



def call(args){

	String roleID = "d2ad2ecf-7105-168b-6b15-5e4c56d63f10"
	String vaultToken = args
	
	//get secret ID
	String output = sh(script: """ 
		cd ~/
		export VAULT_ADDR='http://127.0.0.1:8200'
		./vault login '$vaultToken'
		./vault write -field=secret_id -f auth/approle/role/vault-test/secret-id
	""", returnStdout: true)
	
	//remove unnecessary output from previous shell command
	int index = output.lastIndexOf('\n')
	String secretID = output.substring(index+1).trim()
	
	//retrieve token to access secrets using roleID and secretID
	String login = sh(script: """
		cd ~/
		export VAULT_ADDR='http://127.0.0.1:8200'
		./vault write -field=token auth/approle/login role_id='$roleID' secret_id='$secretID'
	""", returnStdout:true)
	sh """
		cd ~/
		./vault kv get -field=test secret/hello > output.txt
	"""
	//	./vault kv get -field=test secret/hello > output.txt
	//	"""

}

