package hmda.validation

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.parser.fi.FIDataCsvParser
import hmda.validation.engine.ValidationError
import hmda.validation.engine.lar.validity.LarValidityEngine
import hmda.validation.engine.ts.syntactical.TsSyntacticalEngine
import hmda.validation.engine.ts.validity.TsValidityEngine
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ MustMatchers, PropSpec }

import scala.concurrent.{ ExecutionContext, Future }
import scala.io.Source
import scalaz.{ NonEmptyList, ValidationNel }

object RunAFile extends App with TsSyntacticalEngine with TsValidityEngine with LarValidityEngine with MustMatchers {

  // these are needed for TsSyntacticalEngine
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  override def findTimestamp: Future[Long] = Future(201301111330L)
  override def findYearProcessed: Future[Int] = Future(2015)
  override def findControlNumber: Future[String] = Future("")

  override def validate(ts: TransmittalSheet) = {
    throw new NotImplementedError("please call validate method for the specific Engine you want")
  }

  // NOW OUR ACTUAL CODE YAY

  val lineSource = Source.fromFile("parser/src/test/resources/txt/THE_LYONS_NATIONAL_BANK.txt").getLines()
  val fiData = new FIDataCsvParser().parseLines(lineSource.toIterable)

  println(fiData.ts.respondent.name)
  println(fiData.lars.size)

  val fTsSyntacticValidation = super[TsSyntacticalEngine].validate(fiData.ts)
  ScalaFutures.whenReady(fTsSyntacticValidation) {
    println(_)
  }

  val tsValidityValidation = super[TsValidityEngine].validate(fiData.ts)
  println(tsValidityValidation)

  val larValidations = fiData.lars.map(super[LarValidityEngine].validate)
  println(larValidations.size)
  larValidations.size mustBe 1077
  val firstResult: RunAFile.LarValidation = larValidations.iterator.next()
  firstResult.isFailure mustBe true
  private val validationNel: ValidationNel[NonEmptyList[ValidationError], LoanApplicationRegister] = firstResult.toValidationNel
  validationNel.leftSide.leftSide.leftSide.leftSide.leftSide.leftSideValue.leftSideValue
  validationNel.leftMap((x: NonEmptyList[NonEmptyList[ValidationError]]) => x.head.size).valueOr(identity) mustBe 3

  println(new ValidationError("hello"))
}

class IntegrationSpec extends PropSpec {
  property("will this work") {
    RunAFile.main(new Array[String](0))
  }
}
