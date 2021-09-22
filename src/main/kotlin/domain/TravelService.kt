package domain

import org.springframework.stereotype.Component
import outcoming.GMapsService

@Component
class TravelService(
    val travelRequestRepository: TravelRequestRepository,
    val gMapsService: GMapsService
) {

    val MAX_TRAVEL_TIME: Int = 600

    fun saveTravelRequest(travelRequest: TravelRequest) =
        travelRequestRepository.save(travelRequest)

    fun listNearbyTravelRequests(currentAddress: String): List<TravelRequest> {
        val requests = travelRequestRepository.findByStatus(TravelRequestStatus.CREATED)
        return requests.filter {
                tr -> gMapsService.getDistanceBetweenAddresses(currentAddress, tr.origin) < MAX_TRAVEL_TIME
        }
    }
}