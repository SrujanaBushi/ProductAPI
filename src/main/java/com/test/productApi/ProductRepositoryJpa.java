package com.test.productApi;
import jakarta.transaction.Transactional;
import org.hibernate.annotations.SQLUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Date;
import java.util.List;

@Repository
public interface ProductRepositoryJpa extends JpaRepository<Product, Integer> {

    @Query(value="select p.* from product p where p.status='active' order by posted_date DESC ",nativeQuery = true)
    public List<Product> findProductByStatus(String status);
    //@Query(value="select p.* from product p where p.name= :productName or p.posted_date between :minPostedDate and :maxPostedDate or p.price between :minPrice and :maxPrice",nativeQuery = true)
    @Query(value="SELECT p.* FROM Product p " +
            "WHERE (:productName is null or p.name = :productName) " +
            "AND (:minPrice is null or p.price >= :minPrice) " +
            "AND (:maxPrice is null or p.price <= :maxPrice) " +
            "AND (:minPostedDate is null or p.posted_date >= :minPostedDate) " +
            "AND (:maxPostedDate is null or p.posted_date <= :maxPostedDate)",nativeQuery = true)
     List<Product> findProductBySearch( @Param("productName") String productName,
                                              @Param("minPrice") Double minPrice,
                                              @Param("maxPrice") Double maxPrice,
                                              @Param("minPostedDate") Date minPostedDate,
                                              @Param("maxPostedDate") Date maxPostedDate);
    @Modifying
    @Transactional
    @Query(value="update product set name= :productName, price = :price, status= :status where id=:productId",nativeQuery = true)
    public int updateProductById(String productName,
                                  Double price,
                                  String status,
                                  int productId);

    @Query(value="select price from product where id = :productId",nativeQuery = true)
    public Double getProductPriceByID(int productId);

    @Query(value="select * from product where id = :productId",nativeQuery = true)
    public Product getProductByID(int productId);
@Modifying
@Transactional
    @Query(value="delete from product where id = :productId",nativeQuery = true)
    public int deleteProductById(int productId);

}
