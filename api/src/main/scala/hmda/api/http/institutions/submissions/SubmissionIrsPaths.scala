package hmda.api.http.institutions.submissions

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol, SubmissionProtocol }
import spray.json.{ JsBoolean, JsFalse, JsObject, JsTrue }
import java.io.File

import hmda.model.fi.{ IRSGenerated, IRSVerified }

import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.util.Try

trait SubmissionIrsPaths
    extends InstitutionProtocol
    with SubmissionProtocol
    with ApiErrorProtocol
    with EditResultsProtocol
    with HmdaCustomDirectives
    with ValidationErrorConverter {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<submissionId>/irs
  // NOTE:  This is currently a mocked, static endpoint
  def submissionIrsPath(institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber / "irs") { (period, submissionId) =>
      extractExecutionContext { executor =>
        timedGet { uri =>
          implicit val ec: ExecutionContext = executor
          val supervisor = system.actorSelection("/user/supervisor")

          //To avoid having to deal with relative paths on different systems
          val irsJson = "{\n  \"msas\": [\n    {\n      \"id\": \"123\",\n      \"name\": \"MSA 123\",\n      \"totalLARS\": 4,\n      \"totalAmount\": 123,\n      \"conv\": 4,\n      \"FHA\": 0,\n      \"VA\": 0,\n      \"FSA\": 0,\n      \"1to4Family\": 4,\n      \"MFD\": 0,\n      \"multiFamily\": 0,\n      \"homePurchase\": 0,\n      \"homeImprovement\": 0,\n      \"refinance\": 4\n    },\n    {\n      \"id\": \"456\",\n      \"name\": \"MSA 456\",\n      \"totalLARS\": 5,\n      \"totalAmount\": 456,\n      \"conv\": 5,\n      \"FHA\": 0,\n      \"VA\": 0,\n      \"FSA\": 0,\n      \"1to4Family\": 5,\n      \"MFD\": 0,\n      \"multiFamily\": 0,\n      \"homePurchase\": 0,\n      \"homeImprovement\": 0,\n      \"refinance\": 5\n    }\n  ],\n  \"timestamp\": 0,\n  \"receipt\": \"\",\n  \"status\": {\n       \"code\": 10,\n       \"message\": \"IRS report generated\"\n     }}"
          val response = HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, irsJson))

          complete(response)
        } ~
          timedPost { uri =>
            entity(as[JsObject]) { json =>
              val verified = json.fields("verified").asInstanceOf[JsBoolean]
              verified match {
                case JsTrue => complete(ToResponseMarshallable(Receipt(System.currentTimeMillis(), "receiptHash", IRSVerified)))
                case JsFalse => complete(ToResponseMarshallable(Receipt(0L, "", IRSGenerated)))
              }
            }
          }
      }
    }
}
