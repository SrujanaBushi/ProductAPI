package com.test.productApi;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class RetailController {

    @Autowired
    private ProductRepositoryJpa productRepository;
    @Autowired
    private ApprovalRepository approvalRepository;
// To get the products with latest one first
    @GetMapping("/api/products")
    public  Iterable<Product> getproducts() {
        return productRepository.findProductByStatus("active");
    }
// To get the products based on the given column values
    @GetMapping("/api/products/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam(required = false) String productName,
                                        @RequestParam(required = false) Double minPrice,
                                        @RequestParam(required = false) Double maxPrice,
                                        @RequestParam(required = false) Date minPostedDate,
                                        @RequestParam(required = false) Date maxPostedDate) {

        try {
            return ResponseEntity.ok(productRepository.findProductBySearch(productName, minPrice, maxPrice, minPostedDate, maxPostedDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    //Creating an product if the price is less than 5000, pushing it to approval queue if the price is more than 5000
    @PostMapping("/api/products")
    public ResponseEntity<String> addproduct(@RequestBody @Valid Product productdetails) {
       try{
           Product newitem=new Product();
           newitem.setId(productdetails.getId());
           newitem.setName(productdetails.getName());
           newitem.setPrice(productdetails.getPrice());
           newitem.setStatus(productdetails.getStatus());
           newitem.setPostedDate(new Date(System.currentTimeMillis()));
           if(productdetails.getPrice()>=5000) {
               ApprovalQueue approvalItem = new ApprovalQueue();
               approvalItem.setId(productdetails.getId());
               approvalItem.setName(productdetails.getName());
               approvalItem.setPrice(productdetails.getPrice());
               approvalItem.setPostedDate(newitem.getPostedDate());
               approvalItem.setStatus(productdetails.getStatus());
               approvalItem.setApprovalStatus("pending");
               approvalItem.setApprovalRequestDate(new Date(System.currentTimeMillis()));
               approvalItem.setRequestType("Insert");
               approvalRepository.save(approvalItem);
               return ResponseEntity.ok("Price is greater than 5000 waiting for approval") ;
           }else {
               productRepository.save(newitem);
               return ResponseEntity.ok("Product is created");
           }
       } catch(Exception e)
           {
               return ResponseEntity.badRequest().body(e.getMessage());
           }
    }

    /*Updating the product only if the price is less than 50% of the previous price or else move to approval queue
    with request type as update*/
    @PutMapping("/api/products/{productId}")
    public  ResponseEntity<String> updateProduct(@PathVariable int productId ,@RequestBody Product productdetails) {
        try {
            if(productdetails.getPrice() >= (productRepository.getProductPriceByID(productId) * 1.5))
            {
                ApprovalQueue approvalItem=new ApprovalQueue();
                Product productdata=productRepository.getProductByID(productId);
                approvalItem.setId(productId);
                approvalItem.setName(productdetails.getName());
                approvalItem.setPrice(productdetails.getPrice());
                approvalItem.setPostedDate(productdata.getPostedDate());
                approvalItem.setStatus(productdetails.getStatus());
                approvalItem.setApprovalStatus("pending");
                approvalItem.setApprovalRequestDate(new Date(System.currentTimeMillis()));
                approvalItem.setRequestType("Update");
                approvalRepository.save(approvalItem);
                return ResponseEntity.ok("Product Awaiting Approval");
            }else {
                productRepository.updateProductById(productdetails.getName(), productdetails.getPrice(), productdetails.getStatus(), productId);
                return ResponseEntity.ok("Product Updated");
            }
        }
        catch (NullPointerException e){
            return ResponseEntity.badRequest().body("Product Not Found as product id is null");
        }
    }
    //Deleting the product and moving to approval queue with request type as Delete.
    @DeleteMapping("/api/products/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable int productId) {
        try{
            ApprovalQueue approvalQueueItem=new ApprovalQueue();
            Product product=productRepository.getProductByID(productId);
            approvalQueueItem.setPostedDate(product.getPostedDate());
            approvalQueueItem.setId(productId);
            approvalQueueItem.setPrice(product.getPrice());
            approvalQueueItem.setApprovalStatus("pending");
            approvalQueueItem.setName(product.getName());
            approvalQueueItem.setApprovalRequestDate(new Date(System.currentTimeMillis()));
            approvalQueueItem.setStatus(product.getStatus());
            approvalQueueItem.setRequestType("Delete");
            productRepository.deleteProductById(productId);
            approvalRepository.save(approvalQueueItem);
            return ResponseEntity.ok("Product has deleted from the product table and move for the approval");
        }
        catch(NullPointerException e){
            return ResponseEntity.badRequest().body("Product Not Found as product id is null");
        }
        }


// Getting the list of approval queue wih the latest one first
    @GetMapping("/api/products/approval-queue")
    public  Iterable<ApprovalQueue> getApprovals() {
        return approvalRepository.getApprovalsorderedbydate();
    }
/* Approving the items in the approval queue based on the request type
request type : Insert/update : Inserting the record into product table and deleting in approval queue
request type : Delete : Deleting from the approval queue
 */
    @PutMapping("/api/products/approval-queue/{approvalId}/approve")
    public ResponseEntity<String> approve(@PathVariable int approvalId) {
        try {
            ApprovalQueue approvalItem = approvalRepository.getApprovalItemById(approvalId);
            if (approvalItem.getRequestType().equals("Delete")) {
                approvalRepository.removeApprovalItembyId(approvalId);
                return ResponseEntity.ok("Requested has been approved and the product is deleted");
            } else if (approvalItem.getRequestType().equals("Update") || approvalItem.getRequestType().equals("Insert")) {
                Product productitem = new Product();
                productitem.setPrice(approvalItem.getPrice());
                productitem.setId(approvalItem.getId());
                productitem.setName(approvalItem.getName());
                productitem.setStatus(approvalItem.getStatus());
                productitem.setPostedDate(approvalItem.getPostedDate());
                productRepository.save(productitem);
                approvalRepository.removeApprovalItembyId(approvalId);
                return ResponseEntity.ok("Request has been approved and the product is updated");
            }else{
                 return ResponseEntity.ok("Request doesn't have any Request Type");
            }
        } catch(NullPointerException e){
            return ResponseEntity.badRequest().body("Approval id is null");
        }
        }
    /* Approving the items in the approval queue based on the request type
    request type : Delete : Inserting the record into product table and deleting in approval queue
    request type :  Insert/Update : Deleting from the approval queue
     */
    @PutMapping("/api/products/approval-queue/{approvalId}/reject")
    public ResponseEntity<String> reject(@PathVariable int approvalId) {
        try{
            ApprovalQueue approvalItem=approvalRepository.getApprovalItemById(approvalId);
            if(approvalItem.getRequestType().equals("Update") || approvalItem.getRequestType().equals("Insert"))
            {
                approvalRepository.removeApprovalItembyId(approvalId);
                return ResponseEntity.ok("Request is rejected and product is removed from approval queue");
            } else if (approvalItem.getRequestType().equals("Delete")) {
                Product productitem=new Product();
                productitem.setPrice(approvalItem.getPrice());
                productitem.setId(approvalItem.getId());
                productitem.setName(approvalItem.getName());
                productitem.setStatus(approvalItem.getStatus());
                productitem.setPostedDate(approvalItem.getPostedDate());
                productRepository.save(productitem);
                approvalRepository.removeApprovalItembyId(approvalId);
                return ResponseEntity.ok("Request is rejected and product is moved back to the product table and removed form approval queue");
            }else{
                return ResponseEntity.ok("Request doesn't have any Request Type");
            }
        }
        catch(NullPointerException e)
        {
            return ResponseEntity.badRequest().body("Approval Id can't be null");
        }

    }
}