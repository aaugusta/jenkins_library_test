import groovy.json.JsonSlurperClassic



def call(args){

	String vaultToken = args
	HashMap roleMap =	["jenkins": 		"d2ad2ecf-7105-168b-6b15-5e4c56d63f10",
					 "alt-jenkins": 	"7b315cba-a923-cdad-33f9-20923b8fd27d"]
	HashMap pathMap =	["jenkins": 		"auth/approle/role/vault-test/secret-id",
					 "alt-jenkins": 	"auth/approle/role/alt-vault/secret-id"]
	HashMap secretMap =	["jenkins": 	"secret/hello",
					 "alt-jenkins": "secret/goodbye"]

	sh(script: """
		set +x
		cd ~/
		export VAULT_ADDR='http://127.0.0.1:8200'
		./vault login '$vaultToken' > /dev/null
		touch tempfile.JSON
		./vault token lookup -format=json > tempfile.JSON
	""", returnStdout:true)
	
	/*
		retrieves policies attached to the user-supplied token
		these policies will tell us what Role ID the user should be associated with
	*/
	def info
	def policies
	String policy = "default"
	try{
		def tokenInfo = sh(script: "cat ~/tempfile.JSON", returnStdout: true)
		def jsonSlurper = new JsonSlurperClassic()
		info = jsonSlurper.parseText(tokenInfo)
		policies = info.data.policies
		for(int i = 0; i < policies.size(); i++) {
			if(!policies[i].equals("default")){
				policy = policies[i]
			}	
		}
	} catch(Exception e){
		println(e.message())
	}

	
	
	println policy
	String roleID = roleMap.get(policy)
	String secretIDPath = pathMap.get(policy)
	String secretDest = secretMap.get(policy)
	println roleID




	//get secret ID
	String secretID = sh(script: """ 
		set +x
		cd ~/
		export VAULT_ADDR='http://127.0.0.1:8200'	
		./vault login '$vaultToken' > /dev/null
		./vault write -field=secret_id -f '$secretIDPath'
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
		./vault kv get -field=test '$secretDest'
	""", returnStdout:true)

	sh(script: """
		set +x
		touch ~/output.txt
		echo '$secret' >> ~/output.txt
	 """, returnStdout: true)




}

