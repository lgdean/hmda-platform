package hmda.validation.rules.lar.quality

import hmda.parser.fi.lar.LarGenerators
import org.scalacheck.Gen
import org.scalatest.WordSpec
import org.scalatest.prop.PropertyChecks

class Q030WordSpec extends WordSpec with PropertyChecks with LarGenerators {

  "Q030" when {
    "action taken type is 7 or 8 (preapproval)" must {
      val actionTaken = Gen.oneOf(7, 8)
      "pass" in {
        //pass
      }
    }

    "action taken is 1-6" when {
      val actionTaken = Gen.oneOf(1 to 6)

      "institution is NOT a CRA reporter" when {
        "all 4 geography fields are NA" must {
          //pass
        }
      }
      "institution is a CRA reporter" when {
        "all 4 geography fields are NA" must {
          //fail -- is it worth calling this case out explicitly, even though the next one covers it?
        }
        "state or county is NA (no matter what else is true)" must {
          //fail
        }
      }

      "property MSA/MD is present" when {
        "tract is NA" when {
          "MSA/MD, state and county match" must {
            //pass -- Does this include checking if county is large? If county is large, then tract should be reported.
          }
          "MSA/MD, state and county do not match" must {
            //fail
            // I'm thinking this will include the case where state is NA, but is it better to make it separate?
            // same question applies to the other "do not match" cases; they should probably be consistent.
          }
        }
        "tract is present" when {
          "MSA/MD, state, county, and tract match" must {
            //pass -- Does this include when the filer is not a CRA filer and reports a tract in a small county?
          }
          "MSA/MD, state, county, and tract do not match" must {
            //fail
          }
        }
      }

      "property MSA/MD is NA" when {
        val msa = "NA"
        "tract is NA" when {
          "state and county match" must {
            //pass
          }
          "state and county do not match" must {
            //fail
          }
        }
        "tract is present" when {
          "state, county, and tract match" must {
            //pass--Does this include when the filer is not a CRA filer and reports a tract in a small county?
          }
          "state, county, and tract do not match" must {
            //fail
          }
        }
      }

    }
  }

}
