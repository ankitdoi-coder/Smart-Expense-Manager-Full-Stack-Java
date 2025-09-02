package com.ankit.Expence.Tracker.App.Repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ankit.Expence.Tracker.App.Model.Expence;
import com.ankit.Expence.Tracker.App.Model.User;

@Repository
public interface ExpenceRepository extends JpaRepository<Expence, Long> {
	List<Expence> findByUserId(Long userId); // Custom method to get all expenses by user

	long countByUser(User user);

	// Distinct category for the dropdown
	@Query("SELECT DISTINCT e.category FROM Expence e WHERE e.user = :user")
	List<String> findDistinctCategories(@Param("user") User user);

	// fetches the total expense grouped by category
	@Query("""
			    SELECT e.category, SUM(e.amount)
			    FROM Expence e
			    WHERE e.user = :user
			      AND (:fromDate IS NULL OR e.date >= :fromDate)
			      AND (:toDate   IS NULL OR e.date <= :toDate)
			      AND (:category IS NULL OR :category = '' OR e.category = :category)
			      AND (:minAmt   IS NULL OR e.amount >= :minAmt)
			      AND (:maxAmt   IS NULL OR e.amount <= :maxAmt)
			    GROUP BY e.category
			    ORDER BY SUM(e.amount) DESC
			""")
	List<Object[]> sumByCategory(@Param("user") User user,
			@Param("fromDate") Date fromDate,
			@Param("toDate") Date toDate,
			@Param("category") String category,
			@Param("minAmt") Double minAmt,
			@Param("maxAmt") Double maxAmt);

	@Query("""
			    SELECT e.date, SUM(e.amount)
			    FROM Expence e
			    WHERE e.user = :user
			      AND (:fromDate IS NULL OR e.date >= :fromDate)
			      AND (:toDate   IS NULL OR e.date <= :toDate)
			      AND (:category IS NULL OR :category = '' OR e.category = :category)
			      AND (:minAmt   IS NULL OR e.amount >= :minAmt)
			      AND (:maxAmt   IS NULL OR e.amount <= :maxAmt)
			    GROUP BY e.date
			    ORDER BY e.date ASC
			""")
	List<Object[]> sumByDate(@Param("user") User user,
			@Param("fromDate") Date fromDate,
			@Param("toDate") Date toDate,
			@Param("category") String category,
			@Param("minAmt") Double minAmt,
			@Param("maxAmt") Double maxAmt);

	@Query("""
			    SELECT COALESCE(SUM(e.amount), 0)
			    FROM Expence e
			    WHERE e.user = :user
			      AND (:fromDate IS NULL OR e.date >= :fromDate)
			      AND (:toDate   IS NULL OR e.date <= :toDate)
			      AND (:category IS NULL OR :category = '' OR e.category = :category)
			      AND (:minAmt   IS NULL OR e.amount >= :minAmt)
			      AND (:maxAmt   IS NULL OR e.amount <= :maxAmt)
			""")
	Double sumTotal(@Param("user") User user,
			@Param("fromDate") Date fromDate,
			@Param("toDate") Date toDate,
			@Param("category") String category,
			@Param("minAmt") Double minAmt,
			@Param("maxAmt") Double maxAmt);

	// query for mostSpendingcategory beacause hibernate can not understands that
	@Query("SELECT e.category, SUM(e.amount) " +
			"FROM Expence e " +
			"WHERE e.user = :user " +
			"GROUP BY e.category " +
			"ORDER BY SUM(e.amount) DESC")
	List<Object[]> findCategorySpending(@Param("user") User user);

	Expence findTopByUserOrderByIdDesc(User user);

	// for pie chart category wise expence
	@Query("SELECT e.category, SUM(e.amount) FROM Expence e WHERE e.user = :user GROUP BY e.category")
	List<Object[]> getCategoryWiseSpending(@Param("user") User user);

	List<Expence> findByUser(User user);

	// get all expenses for a user ordered newest -> oldest
	List<Expence> findByUserOrderByDateDesc(User user);

	// simple category totals (category, sum)
	@Query("SELECT e.category, SUM(e.amount) " +
			"FROM Expence e " +
			"WHERE e.user = :user " +
			"GROUP BY e.category " +
			"ORDER BY SUM(e.amount) DESC")
	List<Object[]> sumByCategorySimple(@Param("user") User user);

	// optional: get expences for a single category
	List<Expence> findByUserAndCategoryOrderByDateDesc(User user, String category);

	List<Expence> findByCategory(String category);

	//to get the total monthly expence
	@Query("SELECT SUM(e.amount) FROM Expence e " +
			"WHERE e.user.id = :userId " +
			"AND FUNCTION('MONTH', e.date) = :month " +
			"AND FUNCTION('YEAR', e.date) = :year")
	Double findMonthlyTotalByUser(@Param("userId") Long userId,
			@Param("month") int month,
			@Param("year") int year);


    //FIND THE TOTAL OF ALL EXPENCE THAT IN DB HAs
    @Query("SELECT SUM(e.amount) FROM Expence e WHERE e.user.id = :userId")
    Double totalExpence(@Param("userId") Long userId);
}
