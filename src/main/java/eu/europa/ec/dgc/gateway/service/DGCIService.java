package eu.europa.ec.dgc.gateway.service;

import eu.europa.ec.dgc.gateway.entity.DGCIEntity;
import eu.europa.ec.dgc.gateway.repository.DGCIRepository;
import eu.europa.ec.dgc.gateway.restapi.dto.DGCIInit;
import eu.europa.ec.dgc.gateway.restapi.dto.DGCIObject;
import eu.europa.ec.dgc.gateway.restapi.dto.IssueData;
import eu.europa.ec.dgc.gateway.restapi.dto.SignatureData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DGCIService {
  private final DGCIRepository dgciRepository;
  private final TANService tanService;

  public DGCIObject initDGCI(DGCIInit dgciInit) {
    DGCIEntity dgciEntity = new DGCIEntity();
    dgciRepository.saveAndFlush(dgciEntity);
    // TODO how create the DGCI identifier
    return new DGCIObject(dgciEntity.getId(),"did:web:authority:dgci:V1:DE:blabla:"+dgciEntity.getId());
  }

  public SignatureData finishDGCI(long dgciId, IssueData issueData) {
    return new SignatureData("34932382303049", "HSDKLDSSDMMLERIOERMFMKWESMKKWESDSKDKJ== (BASE64)");
  }
}
