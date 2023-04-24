package server.monster.attributes.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import server.common.attributes.service.AttributeService;
import server.monster.attributes.model.MonsterAttributes;
import server.monster.attributes.repository.MonsterAttributeRepository;

import java.util.Map;

@Singleton
public class MonsterAttributeService {

    @Inject
    MonsterAttributeRepository monsterAttributeRepository;

    public Single<MonsterAttributes> createMobAttributes(String mobInstanceId, Map<String, Integer> attributes) {
        AttributeService.validateAttributes(attributes);
        MonsterAttributes mobAttributes = MonsterAttributes.builder()
                .baseAttributes(attributes)
                .currentAttributes(attributes)
                .mobInstanceId(mobInstanceId)
                .build();

        return monsterAttributeRepository.insertMonsterAttributes(mobAttributes);
    }

    public void updateMobCurrentAttributes(String mobInstanceId, Map<String, Integer> attributes) {
        AttributeService.validateAttributes(attributes);
        monsterAttributeRepository.updateMobCurrentAttributes(mobInstanceId, attributes);
    }
}
