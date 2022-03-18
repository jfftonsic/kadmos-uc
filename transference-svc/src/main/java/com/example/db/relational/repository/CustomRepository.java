package com.example.db.relational.repository;

import com.example.TransferenceConfigurationProperties;
import com.example.db.dataobject.TransferenceCreationDO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CustomRepository {
    EntityManager em;
//    JdbcTemplate jdbcTemplate;
    TransferenceConfigurationProperties properties;

    public void transferenceCreation(TransferenceCreationDO transferenceCreationDO) {
        final var nativeQuery = em.createNativeQuery(properties.queries().transferenceCreation());
        // seems a waste to repeatedly create a query object from the sql string everytime.
        // maybe see if the PreparedStatementCreatorFactory can be used here?
        final var singleResult = nativeQuery
                .setParameter("transferenceId", transferenceCreationDO.transferenceId())
                .setParameter("transferenceIdemCode", transferenceCreationDO.transferenceIdemCode())
                .setParameter("transferenceIdemActor", transferenceCreationDO.transferenceIdemActor())
                .setParameter("transferenceSourceBalance", transferenceCreationDO.transferenceSourceBalance())
                .setParameter("transferenceDestinationBalance", transferenceCreationDO.transferenceDestinationBalance())
                .setParameter("transferenceAmount", transferenceCreationDO.transferenceAmount())
                .setParameter("transferenceDebitRequestId", transferenceCreationDO.transferenceDebitRequestId())
                .setParameter("transferenceDebitRequestReqIdemCode",
                        transferenceCreationDO.transferenceDebitRequestReqIdemCode())
                .setParameter("transferenceDebitRequestReqIdemActor",
                        transferenceCreationDO.transferenceDebitRequestReqIdemActor())
                .setParameter("transferenceCreditRequestId", transferenceCreationDO.transferenceCreditRequestId())
                .setParameter("transferenceCreditRequestReqIdemCode",
                        transferenceCreationDO.transferenceCreditRequestReqIdemCode())
                .setParameter("transferenceCreditRequestReqIdemActor",
                        transferenceCreationDO.transferenceCreditRequestReqIdemActor())
                .getSingleResult();
        log.info("result from transference creation {}", singleResult);
    }
}
