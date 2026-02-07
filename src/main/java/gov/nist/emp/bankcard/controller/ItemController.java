package gov.nist.emp.bankcard.controller;

import gov.nist.emp.bankcard.entity.Item;
import gov.nist.emp.bankcard.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * REST controller for Item-related endpoints.
 */
@RestController
@RequestMapping("/api/items")
@Tag(name = "Items", description = "Purchase item management endpoints")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{itemId}")
    @Operation(summary = "Get item by ID")
    public ResponseEntity<Item> getItem(@PathVariable Integer itemId) {
        return itemService.findById(itemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/request/{requestId}")
    @Operation(summary = "Get all items for a request")
    public ResponseEntity<List<Item>> getItemsByRequestId(@PathVariable Integer requestId) {
        return ResponseEntity.ok(itemService.findByRequestId(requestId));
    }

    @PostMapping
    @Operation(summary = "Create a new item")
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        Item saved = itemService.save(item);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "Update an existing item")
    public ResponseEntity<Item> updateItem(@PathVariable Integer itemId, @RequestBody Item item) {
        item.setId(itemId);
        Item saved = itemService.save(item);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Delete an item")
    public ResponseEntity<?> deleteItem(@PathVariable Integer itemId) {
        itemService.deleteById(itemId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{itemId}/project-task/{projectTask}")
    @Operation(summary = "Update project task for an item")
    public ResponseEntity<?> updateProjectTask(@PathVariable Integer itemId, @PathVariable String projectTask) {
        // TODO: Implement
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{itemId}/object-class/{objectClass}")
    @Operation(summary = "Update object class for an item")
    public ResponseEntity<?> updateObjectClass(@PathVariable Integer itemId, @PathVariable String objectClass) {
        // TODO: Implement
        return ResponseEntity.ok().build();
    }

    @PostMapping("/shopping-cart")
    @Operation(summary = "Import items from shopping cart file")
    public ResponseEntity<?> importFromShoppingCart(@RequestParam("file") MultipartFile file) {
        // TODO: Implement
        return ResponseEntity.ok().build();
    }

    @PostMapping("/csv")
    @Operation(summary = "Import items from CSV file")
    public ResponseEntity<?> importFromCsv(@RequestParam("file") MultipartFile file) {
        // TODO: Implement
        return ResponseEntity.ok().build();
    }
}
