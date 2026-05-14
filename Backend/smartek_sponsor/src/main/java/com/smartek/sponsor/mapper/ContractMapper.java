package com.smartek.sponsor.mapper;

import com.smartek.sponsor.dto.ContractDTO;
import com.smartek.sponsor.entity.Contract;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {
    
    public ContractDTO toDTO(Contract contract) {
        if (contract == null) {
            return null;
        }
        
        return ContractDTO.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .amount(contract.getAmount())
                .currency(contract.getCurrency())
                .description(contract.getDescription())
                .status(contract.getStatus())
                .type(contract.getType())
                .sponsorId(contract.getSponsor() != null ? contract.getSponsor().getId() : null)
                .sponsorName(contract.getSponsor() != null ? contract.getSponsor().getName() : null)
                .sponsorEmail(contract.getSponsor() != null ? contract.getSponsor().getEmail() : null)
                .build();
    }
    
    public Contract toEntity(ContractDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Contract contract = new Contract();
        contract.setId(dto.getId());
        contract.setContractNumber(dto.getContractNumber());
        contract.setStartDate(dto.getStartDate());
        contract.setEndDate(dto.getEndDate());
        contract.setAmount(dto.getAmount());
        contract.setCurrency(dto.getCurrency());
        contract.setDescription(dto.getDescription());
        contract.setStatus(dto.getStatus());
        contract.setType(dto.getType());
        
        return contract;
    }
}
