package com.smartek.sponsor.service.impl;

import com.smartek.sponsor.entity.Contract;
import com.smartek.sponsor.entity.Sponsor;
import com.smartek.sponsor.exception.ResourceNotFoundException;
import com.smartek.sponsor.repository.ContractRepository;
import com.smartek.sponsor.repository.SponsorRepository;
import com.smartek.sponsor.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;
    private final SponsorRepository sponsorRepository;

    @Override
    public Contract createContract(Long sponsorId, Contract contract) {
        Sponsor sponsor = sponsorRepository.findById(sponsorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsor", "id", sponsorId));
        contract.setId(null);
        contract.setSponsor(sponsor);
        return contractRepository.save(contract);
    }

    @Override
    public List<Contract> getAllContracts() {
        return contractRepository.findAll();
    }

    @Override
    public Contract getContractById(Long contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));
    }

    @Override
    public Contract updateContract(Long contractId, Long sponsorId, Contract contract) {
        Contract existing = getContractById(contractId);
        existing.setContractNumber(contract.getContractNumber());
        existing.setStartDate(contract.getStartDate());
        existing.setEndDate(contract.getEndDate());
        existing.setAmount(contract.getAmount());
        existing.setCurrency(contract.getCurrency());
        existing.setDescription(contract.getDescription());
        existing.setStatus(contract.getStatus());
        existing.setType(contract.getType());

        if (sponsorId != null) {
            Sponsor sponsor = sponsorRepository.findById(sponsorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sponsor", "id", sponsorId));
            existing.setSponsor(sponsor);
        }

        return contractRepository.save(existing);
    }

    @Override
    public void deleteContract(Long contractId) {
        Contract existing = getContractById(contractId);
        contractRepository.delete(existing);
    }
}

