/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.trustsstore.services

import akka.stream.actor.ActorPublisherMessage.Request
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.trustsstore.models._
import uk.gov.hmrc.trustsstore.models.requests.IdentifierRequest
import uk.gov.hmrc.trustsstore.repositories.ClaimedTrustsRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton()
class ClaimedTrustsService @Inject()(private val claimedTrustsRepository: ClaimedTrustsRepository)  {

  def get(internalId: String): Future[ClaimedTrustResponse] = {
    claimedTrustsRepository.get(internalId) map {
      case Some(trustClaim) => GetClaimFoundResponse(trustClaim)
      case None => GetClaimNotFoundResponse(Json.obj("errors" -> "No TrustClaim was found for the given for this authenticated internalId"))
    }
  }

  def store(internalId: String, maybeUtr: Option[String], maybeManagedByAgent: Option[Boolean]): Future[ClaimedTrustResponse] = {

    val trustClaim = (maybeUtr, maybeManagedByAgent) match {
      case (Some(utr), Some(managedByAgent)) => Some(TrustClaim(internalId, utr, managedByAgent))
      case _ => None
    }

    trustClaim match {
      case Some(tc) =>
        claimedTrustsRepository.store(tc).map {
          case Left(writeErrors) => StoreErrorsResponse(writeErrors)
          case Right(storedTrustClaim) => StoreSuccessResponse(storedTrustClaim)
        }
      case None => Future.successful(StoreParsingErrorResponse(Json.obj("errors" -> "Unable to parse request body into a TrustClaim")))
    }
  }

}