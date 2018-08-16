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


Jenkins.instance.getAllItems(Folder.class)
    .findAll{}
    .each{
    	println(it.name)
    	sh 'echo "$it.name"'
        sh 'echo found it'
//         AbstractFolder<?> folderAbs = AbstractFolder.class.cast(it)
//         FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)
//         if(property != null){
//             property.getStore().addCredentials(Domain.global(), c)
//             println property.getCredentials().toString()
//         }
 	}
}