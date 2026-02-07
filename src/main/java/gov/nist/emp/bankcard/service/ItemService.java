package gov.nist.emp.bankcard.service;

import gov.nist.emp.bankcard.entity.Item;
import gov.nist.emp.bankcard.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Item-related business logic.
 */
@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Optional<Item> findById(Integer itemId) {
        return itemRepository.findById(itemId);
    }

    public List<Item> findByRequestId(Integer requestId) {
        return itemRepository.findByRequestId(requestId);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Integer itemId) {
        itemRepository.deleteById(itemId);
    }

    public List<Item> findTaggableEquipment() {
        return itemRepository.findTaggableEquipment();
    }

    public List<Item> findChemicalItems() {
        return itemRepository.findChemicalItems();
    }

    // TODO: Add methods for:
    // - importFromShoppingCart
    // - importFromCsv
    // - updateProjectTask
    // - updateObjectClass
    // - processItem
    // - markAsBilled
}
