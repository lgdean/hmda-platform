package hmda.persistence.processing

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.scaladsl._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.persistence.CommonMessages._

object HmdaQuery {

  type RJ = ReadJournal with AllPersistenceIdsQuery with CurrentPersistenceIdsQuery with EventsByPersistenceIdQuery with CurrentEventsByPersistenceIdQuery with EventsByTagQuery with CurrentEventsByTagQuery

  val config = ConfigFactory.load()

  val journalId = config.getString("akka.persistence.query.journal.id")

  def readJournal(system: ActorSystem) = {
    println(s"$journalId")
    PersistenceQuery(system).readJournalFor[RJ](journalId)
  }

  def events(persistenceId: String)(implicit system: ActorSystem, materializer: ActorMaterializer): Source[Event, NotUsed] =
    readJournal(system).currentEventsByPersistenceId(persistenceId, 0L, Long.MaxValue)
      .map(_.event.asInstanceOf[Event])

}

