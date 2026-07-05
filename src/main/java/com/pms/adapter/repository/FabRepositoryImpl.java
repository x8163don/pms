package com.pms.adapter.repository;

import com.pms.adapter.repository.dto.FabDto;
import com.pms.adapter.repository.mapper.FabMapper;
import com.pms.domain.fab.Fab;
import com.pms.usecase.fab.FabRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FabRepositoryImpl implements FabRepository {

    private final FabJpaRepository jpaRepository;

    public FabRepositoryImpl(FabJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Fab> getById(Long id) {
        return jpaRepository.findById(id).map(FabMapper::toDomain);
    }

    @Override
    public Fab save(Fab fab) {
        FabDto dto = FabMapper.toDto(fab);
        FabDto saved = jpaRepository.save(dto);
        return FabMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCityId(Long cityId) {
        return jpaRepository.existsByCityId(cityId);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
}
