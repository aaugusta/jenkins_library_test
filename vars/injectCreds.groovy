import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty
import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import org.jenkinsci.plugins.plaincredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*



def call(projectName, token){

String id = "vault_token"
Credentials c = new StringCredentialsImpl(CredentialsScope.GLOBAL, id, "description: Token for passing to library functions", "$token")


Jenkins.instance.getAllItems(Folder.class)
    .findAll{it.name.equals('$projectName')}
    .each{
        AbstractFolder<?> folderAbs = AbstractFolder.class.cast(it)
        FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)
        if(property != null){
            property.getStore().addCredentials(Domain.global(), c)
            println property.getCredentials().toString()
        }
}
}