import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty
import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import org.jenkinsci.plugins.plaincredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret
import jenkins.model.*



def call(projectName, token){


	sh 'echo starting...'
	String id = "vault_token"
	//println(jenkins.model.Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.CredentialsProvider.all()'))
	Credentials c = new StringCredentialsImpl(CredentialsScope.GLOBAL, id, "description: Token for passing to library functions", Secret.fromString("$token"))

	def items = Jenkins.instance.getAllItems(Folder.class)
	println(items)
	sh 'echo $items'
	for (folder in items) {
		println(folder.name)
		sh 'echo "$folder.name"'
	  //if(folder.name.equals('FolderName')){
		// AbstractFolder<?> folderAbs = AbstractFolder.class.cast(folder)
	 //    FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)
	 //    property.getStore().addCredentials(Domain.global(), c)
	 //    println property.getCredentials().toString()
	  //}
	}


}