package api

import domain.Driver
import domain.DriverRepository
import errorhandling.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Service
@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class DriverApi(
        val driverRepository: DriverRepository
) {

    @GetMapping("/drivers")
    fun listDrivers() = driverRepository.findAll()

    @GetMapping("/drivers/{ID}")
    fun findDriver(@PathVariable("id") id:Long) = driverRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    @PostMapping("drivers")
    fun createDriver(@RequestBody driver: Driver) = driverRepository.save(driver);

    @PutMapping("/drivers/{id}")
    fun fullUpdateDriver(@PathVariable("id") id: Long, @RequestBody driver: Driver):Driver{
        val foundDriver = findDriver(id)
        val copyDriver = foundDriver.copy(
                birthDate = driver.birthDate,
                name = driver.name
        )
        return driverRepository.save(copyDriver);
    }

    @PatchMapping("/drivers/{id}")
    fun incrementalUpdateDriver(@PathVariable("id")id: Long, @RequestBody driver: PatchDriver): Driver{
        val foundDriver = findDriver(id)
        val copyDriver = foundDriver.copy(
                birthDate = driver.birthDate ?: foundDriver.birthDate,
                name = driver.name ?: foundDriver.name
        )
        return driverRepository.save(copyDriver)
    }

    @DeleteMapping("drivers/{id}")
    fun deleteDriver(@PathVariable("id") id: Long) =
            driverRepository.deleteById(id)

}

data class PatchDriver(
        val name: String?,
        val birthDate: LocalDate?
)

@Tag(name = "Driver API", description = "Manipula dados de motoristas.")
interface DriverAPI {


    @Operation(description = "Lista todos os motoristas disponíveis")
    fun listDrivers() : List<Driver>

    @Operation(description = "Localiza um motorista específico", responses = [
        ApiResponse(responseCode = "200", description = "Caso o motorista tenha sido encontrado na base"),
        ApiResponse(responseCode = "404", description = "Caso o motorista não tenha sido encontrado",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    ])
    fun findDriver(@Parameter(description = "ID do motorista a ser localizado") id: Long) : Driver

    fun createDriver(driver: Driver): Driver

    fun fullUpdateDriver(id:Long, driver:Driver) : Driver

    fun incrementalUpdateDriver(id:Long, driver: PatchDriver) : Driver

    fun deleteDriver(@PathVariable("id") id: Long)

}