package com.yebur.backendorderly.resttable;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service("restTableService")
public class RestTableService implements RestTableServiceInterface {

    private final RestTableRepository restTableRepository;

    public RestTableService(RestTableRepository restTableRepository) {
        this.restTableRepository = restTableRepository;
    }

    @Override
    public List<RestTableResponse> findAllRestTableDTO() {
        return getNameOfTableList(restTableRepository.findAllRestTableDTO());
    }

    @Override
    public Optional<RestTable> findById(Long id) {
        return restTableRepository.findById(id);
    }

    @Override
    public Optional<RestTableResponse> findRestTableDTOById(Long id) {
        return restTableRepository.findRestTableDTOById(id).map(this::applyTableNameLogic);
    }

    @Override
    public Optional<RestTableResponse> findRestTableDTOByNumber(int number) {
        return restTableRepository.findRestTableDTOByNumber(number).map(this::applyTableNameLogic);
    }

    @Override
    public RestTableResponse createRestTable(RestTableRequest restTable) {
        RestTable newRestTable = new RestTable();
        newRestTable.setNumber(restTable.getNumber());
        newRestTable.setStatus(TableStatus.valueOf(restTable.getStatus()));
        newRestTable = restTableRepository.save(newRestTable);
        return findRestTableDTOById(newRestTable.getId()).map(this::applyTableNameLogic)
            .orElseThrow(() -> new RuntimeException("Error: id of new RestTable not found"));
    }

    @Override
    public RestTableResponse updateRestTable(Long id, RestTableRequest restTable) {
        RestTable updateTable = findById(id).orElseThrow(() -> new RuntimeException("Table not found with id " + id));

        updateTable.setNumber(restTable.getNumber());
        updateTable.setStatus(TableStatus.valueOf(restTable.getStatus()));
        return findRestTableDTOById(updateTable.getId()).map(this::applyTableNameLogic)
            .orElseThrow(() -> new RuntimeException("Error: id of updated RestTable not found"));
    }

    @Override
    public void deleteRestTable(Long id) {
        if(!restTableRepository.existsById(id)){
            throw new RuntimeException("Table not found with id " + id);
        }
        restTableRepository.deleteById(id);
    }

    private List<RestTableResponse> getNameOfTableList(List<RestTableResponse> tables) {
        for (RestTableResponse table : tables) {
            if (table.getNumber() > 100) {
                table.setName("Mesa T" + (table.getNumber() - 100));
            } else {
                table.setName("Mesa " + table.getNumber());
            }
        }

        return tables;
    }

    private RestTableResponse applyTableNameLogic(RestTableResponse table) {
        if (table.getNumber() > 100) {
            table.setName("Mesa T" + (table.getNumber() - 100));
        } else {
            table.setName("Mesa " + table.getNumber());
        }
        return table;
    }

}
