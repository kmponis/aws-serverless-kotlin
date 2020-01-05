package com.aws.lambda.core

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.amazonaws.services.dynamodbv2.model.*
import ws.osiris.core.ComponentsProvider
import ws.osiris.core.api
import java.text.SimpleDateFormat
import java.util.*
import java.util.Collections.singletonList
import java.util.concurrent.TimeUnit

/** Set on Config.kt */
const val ENVIRONMENT_VARIABLE = "ENVIRONMENT_VARIABLE"

/** The API. */
val api = api<CreatedComponents> {

    // Index url
    get("/") {
        mapOf("message" to "Index page", "environment variable" to environmentVariable)
    }

    // Insert object, params: title=Post_title&description=Post_description&username=kostas&email=user@user.com
    post("/insertPost") {
        val title = it.queryParams["title"]
        val description = it.queryParams["description"]
        val username = it.queryParams["username"]
        val email = it.queryParams["email"]
        val post = Post(
                title = title,
                description = description,
                user = Post.User(username = username, email = email)
        )
        dynamoDBMapper.save(post)
        mapOf("endpoint" to "/insertPost", "post" to post)
    }

    // Retrieve all object, params: none
    get("/getAllPosts") {
        val posts: List<Post?> = dynamoDBMapper.scan(Post::class.java, DynamoDBScanExpression())
        mapOf("endpoint" to "/getAllPosts", "posts" to posts)
    }

    // Retrieve object by HashKey, params: id=75133848-c9c6-4987-b6dc-9d721756178d
    get("/getPostById") {
        val id = it.queryParams["id"]
        val post = dynamoDBMapper.load(Post::class.java, id)
        mapOf("endpoint" to "/getPostById", "post" to post)
    }

    // Retrieve object, params: title=ad
    get("/getPostByTitle") {
        val title = it.queryParams["title"]

        val posts: List<Post?> = dynamoDBMapper.scan(Post::class.java,
                DynamoDBScanExpression().withFilterConditionEntry("title",
                        Condition().withComparisonOperator(ComparisonOperator.EQ)
                                .withAttributeValueList(singletonList(AttributeValue(title)))))

        mapOf("endpoint" to "/getPostByTitle", "posts" to posts)
    }

    // Retrieve nested object, params: username=kostas
    get("/getPostByUsername") {
        val username = it.queryParams["username"]

        // 'user' is the object and 'username' the nested object
        val posts: List<Post?> = dynamoDBMapper.scan(Post::class.java,
                DynamoDBScanExpression()
                        .withFilterExpression("#k_user.#k_username = :v_user_username")
                        .withExpressionAttributeNames(mapOf("#k_user" to "user", "#k_username" to "username"))
                        .withExpressionAttributeValues(mapOf(":v_user_username" to AttributeValue(username))))
        mapOf("endpoint" to "/getPostByUsername", "posts" to posts)
    }
}

/**
 * Creates the components used by the example API.
 */
fun createComponents(): CreatedComponents = CreatedComponentsImpl()

/**
 * A Component interface
 */
interface CreatedComponents : ComponentsProvider {
    val environmentVariable: String
    val dynamoDBMapper: DynamoDBMapper
}

/**
 * A Component implementation
 */
class CreatedComponentsImpl : CreatedComponents {
    override val environmentVariable: String = System.getenv(ENVIRONMENT_VARIABLE) ?:
        "[Environment variable ENVIRONMENT_VARIABLE not set]"
    override val dynamoDBMapper: DynamoDBMapper = createPostTable(AmazonDynamoDBClientBuilder.defaultClient())
}

/**
 * Create Post table on the fly and returning DynamoDBMapper
 */
fun createPostTable(dynamoClient: AmazonDynamoDB): DynamoDBMapper {
    val dynamoDBMapper = DynamoDBMapper(dynamoClient)
    try {
        dynamoClient.createTable(
                dynamoDBMapper.generateCreateTableRequest(Post::class.java)
                        .withBillingMode(BillingMode.PROVISIONED)
                        .withProvisionedThroughput(ProvisionedThroughput(5L, 5L))

        )
        // Wait for Table creation
        TimeUnit.SECONDS.sleep(5L)
    } catch (e: ResourceInUseException) {
        // TODO: Print a log message on API gateway
        println("Table already created.")
    }
    return dynamoDBMapper
}

/**
 * Data class Post including nested class User
 */
@DynamoDBTable(tableName = "Post")
data class Post(
        @DynamoDBHashKey(attributeName="id")
        var id: String = UUID.randomUUID().toString(),

        @DynamoDBAttribute(attributeName = "created_at")
        var createdAt: String? = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),

        @DynamoDBAttribute(attributeName = "title")
        var title: String? = null,

        @DynamoDBAttribute(attributeName = "description")
        var description: String? = null,

        @DynamoDBAttribute(attributeName = "user")
        var user: User? = null
) {
    @DynamoDBDocument
    data class User(
            @DynamoDBAttribute(attributeName = "id")
            var id: String = UUID.randomUUID().toString(),
            @DynamoDBAttribute(attributeName="username")
            var username: String? = null,
            @DynamoDBAttribute(attributeName="email")
            var email: String? = null
    )
}