# AWS-SERVERLESS-KOTLIN
An AWS serverless Kotlin project to save and retrieve 'Post' from DynamoDB

### Included endpoints
* get: /, no params 
* post: /insertPost, params(title, description, username, email)
* get: /getAllPosts, no params
* get: /getPostById, params(id)
* get: /getPostByTitle, params(title)
* get: /getPostByUsername, params(username)

### Osiris plugin 
* The project was created using the bellow command.
<br>`$ mvn archetype:generate -DarchetypeGroupId=ws.osiris -DarchetypeArtifactId=osiris-archetype -DarchetypeVersion=1.4.0`
* The bellow prerequisites were needed:
<br>- JDK8, Maven and Git
<br>- An AWS account.
<br>- An AWS user with administrator permissions, access key and secret access key.
<br>- Added access key and secret access key to system environments. Add following lines to .bash_profile.
<br>`export AWS_ACCESS_KEY_ID="<access_key>"`
<br>`export AWS_SECRET_ACCESS_KEY="<secret_access_key>"`
<br>`export AWS_REGION="<region>"`
* Build and Deploy 
<br>`mvn deploy`

### Git examples
* https://github.com/cjkent/osiris/wiki/Getting-Started-with-Maven
* https://github.com/cjkent/osiris-examples/tree/master/dynamodb
* https://gist.github.com/gaplo917/a4298d755c076b1a295026ed9b3521fa