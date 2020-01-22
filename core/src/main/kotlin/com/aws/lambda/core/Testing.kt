package com.aws.lambda.core

import java.io.FileInputStream
import java.util.*

// Test Functionality
fun main() {
    getKmsKeyId("hello.user")
}

// Get properties value
// DO NOT work with AWS Lambda
fun getKmsKeyId(propertyName: String): String? {
    val contentRootPath = "/Users/konstantinosbonis/Developer/Workspace/Personal/kmponis/aws-serverless-kotlin/core/src/main/resources/resources.properties"
    val fis = FileInputStream(contentRootPath)
    val prop = Properties()
    prop.load(fis)
    return prop.getProperty(propertyName)
}