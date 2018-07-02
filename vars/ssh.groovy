import groovy.json.*



def call(args){

	//String vaultToken = args
	String vaultToken = "af625cbf-1a54-fc57-19d4-28ee49293e12"
	def roleMap = [	"jenkins": "d2ad2ecf-7105-168b-6b15-5e4c56d63f10",
					"alt-jenkins": "7b315cba-a923-cdad-33f9-20923b8fd27d"]
	def pathMap = [	"jenkins": "auth/approle/role/vault-test/secret-id",
					"alt-jenkins": "auth/approle/role/alt-vault/secret-id"]
	def secretMap = [	"jenkins": "secret/hello"
						"alt-jenkins": "secret/goodbye"]

	sh """
		
		cd ~/
		export VAULT_ADDR='http://127.0.0.1:8200'
		./vault login '$vaultToken' > /dev/null
		touch tempfile.JSON
		./vault token lookup -format=json > tempfile.JSON
	"""
	
	/*
		retrieves policies attached to the user-supplied token
		these policies will tell us what Role ID the user should be associated with
	*/
	def tokenInfo = sh(script: "cat ~/tempfile.JSON", returnStdout: true)
	def jsonSlurper = new JsonSlurper()
	def info = jsonSlurper.parseText(tokenInfo)
	def policies = info.data.policies
	
	String policy = "default"
	for(int i = 0; i < policies.size(); i++) {
		if(!policies[i].equals("default")){
			policy = policies[i]
		}
	}
	println policy
	String roleID = roleMap.get(policy)
	String path = pathMap.get(policy)
	String secret = secretMap.get(policy)
	println roleID




	//get secret ID
	String secretID = sh(script: """ 
		set +x
		cd ~/	
		./vault write -field=secret_id -f '$path'
	""", returnStdout: true)
	
	

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
		
		./vault kv get -field=test '$secret'
	""", returnStdout:true)

	sh(script: """
		set +x
		touch ~/output.txt
		echo '$secret1' >> ~/output.txt
		echo '$secret2' >> ~/output.txt
	 """, returnStdout: true)




}

