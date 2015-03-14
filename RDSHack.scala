package com.pdeyhim

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.http.{DefaultErrorResponseHandler, ExecutionContext, StaxResponseHandler}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.rds.AmazonRDSClient
import com.amazonaws.services.rds.model.transform.{DBInstanceStaxUnmarshaller, ModifyDBInstanceRequestMarshaller}
import com.amazonaws.services.rds.model.{DBInstance, ModifyDBInstanceRequest}
import com.amazonaws.transform.{StaxUnmarshallerContext, Unmarshaller}
import com.amazonaws.{AmazonWebServiceRequest, Request, Response}


object RDSHack extends App {

  val creds = new AWSCredentials {
    override def getAWSAccessKeyId: String = "x"
    override def getAWSSecretKey: String = "x"
  }

  val request = new ModifyDBInstanceRequest().withDBInstanceIdentifier("parviztest")

  val myRdsClient = new MyAmazonRDSClient(creds)
  myRdsClient.setRegion(Region.getRegion(Regions.fromName("us-west-2")))

  val res = myRdsClient.modifyDBInstance(request)

}

class MyAmazonRDSClient(credentials:AWSCredentials ) extends AmazonRDSClient {

  override def modifyDBInstance(modifyDBInstanceRequest: ModifyDBInstanceRequest): DBInstance = {
    val executionContext: ExecutionContext = createExecutionContext(modifyDBInstanceRequest)
    val request: Request[ModifyDBInstanceRequest] = new ModifyDBInstanceRequestMarshaller().marshall(super.beforeMarshalling(modifyDBInstanceRequest))
    request.addParameter("CACertificateIdentifier", "rds-ca-2015")
    val response: Response[DBInstance] = invoke(request, new DBInstanceStaxUnmarshaller, executionContext)
    response.getAwsResponse
  }

  private def invoke[X, Y <: AmazonWebServiceRequest] (request: Request[Y], unmarshaller: Unmarshaller[X, StaxUnmarshallerContext], executionContext: ExecutionContext): Response[X] = {
    request.setEndpoint (endpoint)
    request.setTimeOffset (timeOffset)
    val originalRequest: AmazonWebServiceRequest = request.getOriginalRequest

    import scala.collection.JavaConversions._

    for (entry <- originalRequest.copyPrivateRequestParameters.entrySet) {
      request.addParameter (entry.getKey, entry.getValue)
    }
    executionContext.setCredentials (credentials)
    val responseHandler: StaxResponseHandler[X] = new StaxResponseHandler[X] (unmarshaller)
    val errorResponseHandler: DefaultErrorResponseHandler = new DefaultErrorResponseHandler (exceptionUnmarshallers)
    client.execute (request, responseHandler, errorResponseHandler, executionContext)
  }
}

