



def call(args){

	String roleID = "d2ad2ecf-7105-168b-6b15-5e4c56d63f10"
	String vaultToken = args
	
	//get secret ID
	String secretID = sh(script: """ 
		cd ~/
		export VAULT_ADDR='http://127.0.0.1:8200'
		./vault login '$vaultToken' > /dev/null
		./vault write -field=secret_id -f auth/approle/role/vault-test/secret-id
	""", returnStdout: true)
	
	//retrieve token to access secrets using roleID and secretID
	String secretToken = sh(script: """
		cd ~/
		export VAULT_ADDR='http://127.0.0.1:8200'
		./vault write -field=token auth/approle/login role_id='$roleID' secret_id='$secretID'
	""", returnStdout:true)
	
	String secret = sh(script: """
		cd ~/
		set +x
		export VAULT_ADDR='http://127.0.0.1:8200'
		./vault login $secretToken > /dev/null
		touch output.txt
		./vault kv get -field=test secret/hello
	""", returnStdout:true)

	sh "set +x; echo '$secret' > ~/output.txt"

}

