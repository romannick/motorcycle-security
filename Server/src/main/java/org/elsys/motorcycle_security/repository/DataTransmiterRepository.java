package org.elsys.motorcycle_security.repository;

import org.elsys.motorcycle_security.models.DataTransmiter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface DataTransmiterRepository extends CrudRepository<DataTransmiter,Long> {
    @Query("select MAX(dataTrans) from DataTransmiter dataTrans where dataTrans.device.deviceId=:deviceId")
    DataTransmiter getGpsCoordinatesByDeviceId(@Param("deviceId") String deviceId);

    @Query("SELECT MIN(dataTrans) FROM DataTransmiter dataTrans WHERE dataTrans.device.deviceId=:deviceId and dataTrans.date BETWEEN :start AND :endd")
    DataTransmiter getGpsCoordinatesForDay(@Param("deviceId") String deviceId, @Param("start") Date start, @Param("endd") Date endd);
}
