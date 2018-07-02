import groovy.json.*



def call(args){

	String vaultToken = args
	def roleMap = ["[default, jenkins]": "d2ad2ecf-7105-168b-6b15-5e4c56d63f10"]

	//get secret ID
	String secretID = sh(script: """ 
		set +x
		cd ~/
		export VAULT_ADDR='http://127.0.0.1:8200'
		./vault login '$vaultToken' > /dev/null
		touch tempfile.JSON
		./vault token lookup -format=json > tempfile.JSON
		./vault write -field=secret_id -f auth/approle/role/vault-test/secret-id
	""", returnStdout: true)
	
	/*
		retrieves policies attached to the user-supplied token
		these policies will tell us what Role ID the user should be associated with
	*/
	def tokenInfo = sh(script: "cat ~/tempfile.JSON", returnStdout: true)
	def jsonSlurper = new JsonSlurper()
	def info = jsonSlurper.parseText(tokenInfo)
	String roleID = roleMap.get(info.data.policies.toString())
	println roleID
	//retrieve token to access secrets using roleID and secretID
	String secretToken = sh(script: """
		set +x
		cd ~/
		export VAULT_ADDR='http://127.0.0.1:8200'
		./vault write -field=token auth/approle/login role_id='$roleID' secret_id='$secretID'
	""", returnStdout:true)
	
	def secret = sh(script: """
		set +x
		cd ~/
		export VAULT_ADDR='http://127.0.0.1:8200'
		./vault login $secretToken > /dev/null
		touch output.txt
		./vault kv get -field=test secret/hello
	""", returnStdout:true)

	sh(script: "set +x; echo '$secret' > ~/output.txt", returnStdout: true)




}

