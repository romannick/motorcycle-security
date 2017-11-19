package org.elsys.motorcycle_security.business.logic;

import org.elsys.motorcycle_security.SendGPSCords;
import org.elsys.motorcycle_security.models.DataTransmiter;
import org.elsys.motorcycle_security.repository.DataTransmiterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class GPSCordsHandler {

    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private DataTransmiterRepository dataTransmiterRepository;

    public SendGPSCords getLatestCordsForUser(int id){
        return new SendGPSCords(1,1);
    }

    public void testCreate(Long x,Long y){
        DataTransmiter d = new DataTransmiter();
        d.setX(x);
        d.setY(y);

        dataTransmiterRepository.save(d);
    }
}
