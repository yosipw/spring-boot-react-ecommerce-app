package com.ujjaval.ecommerce.commondataservice.service;

import com.ujjaval.ecommerce.commondataservice.dao.sql.categories.*;
import com.ujjaval.ecommerce.commondataservice.dao.sql.images.BrandImagesRepository;
import com.ujjaval.ecommerce.commondataservice.dao.sql.images.CarouselImagesRepository;
import com.ujjaval.ecommerce.commondataservice.dao.sql.images.ApparelImagesRepository;
import com.ujjaval.ecommerce.commondataservice.dao.sql.info.ProductInfoRepository;
import com.ujjaval.ecommerce.commondataservice.entity.sql.categories.PriceRangeCategory;
import com.ujjaval.ecommerce.commondataservice.entity.sql.categories.SortByCategory;
import com.ujjaval.ecommerce.commondataservice.entity.sql.categories.ApparelCategory;
import com.ujjaval.ecommerce.commondataservice.entity.sql.categories.GenderCategory;
import com.ujjaval.ecommerce.commondataservice.entity.sql.categories.ProductBrandCategory;
import com.ujjaval.ecommerce.commondataservice.entity.sql.images.BrandImages;
import com.ujjaval.ecommerce.commondataservice.entity.sql.images.CarouselImages;
import com.ujjaval.ecommerce.commondataservice.entity.sql.images.ApparelImages;
import com.ujjaval.ecommerce.commondataservice.entity.sql.info.ProductInfo;
import com.ujjaval.ecommerce.commondataservice.service.interfaces.LoadFakeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;

@Service
public class LoadFakeDataServiceImpl implements LoadFakeDataService {

    enum FileNameType {
        SORT_BY, PRICE_RANGE
    }


    private final int WOMEN = 1;
    private final int MEN = 2;
    private final int GIRLS = 3;
    private final int BOYS = 4;
    private final int HOME_AND_LIVING = 5;
    private final int ESSENTIALS = 6;

    private final String DATA_DIRECTORY = "fake_data";
    private final String MAIN_SCREEN_DATA = "main-screen-data.txt";
    private final String PRICE_RANGE_DATA = "price-range-data.txt";
    private final String SORT_BY_DATA = "sortby-data.txt";
    private final String WEB_DATA = "web-data.txt";

    @Autowired
    Environment environment;

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Autowired
    private GenderCategoryRepository genderCategoryRepository;

    @Autowired
    private ApparelCategoryRepository apparelCategoryRepository;

    @Autowired
    private ProductBrandCategoryRepository productBrandCategoryRepository;

    @Autowired
    private BrandImagesRepository brandImagesRepository;

    @Autowired
    private ApparelImagesRepository apparelImagesRepository;

    @Autowired
    private CarouselImagesRepository carouselImagesRepository;

    @Autowired
    private SortByCategoryRepository sortByCategoryRepository;

    @Autowired
    private PriceRangeCategoryRepository priceRangeCategoryRepository;

    private String removeSpaces(String str) {
        return str.replaceAll("\\s", "");
    }

    private int generateRandomInt(int max, int min) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public float generateRandomFloat(float leftLimit, float rightLimit, int places) {
        double scale = Math.pow(10, places);
        float decimalNumber = leftLimit + new Random().nextFloat() * (rightLimit - leftLimit);
        return (float) (Math.round(decimalNumber * scale) / scale);
    }

    private Date generateRandomDate() {
        long beginTime = Timestamp.valueOf("2019-01-01 00:00:00").getTime();
        long endTime = Timestamp.valueOf("2020-06-31 00:00:00").getTime();
        long diff = endTime - beginTime + 1;
        return new Date(beginTime + (long) (Math.random() * diff));
    }

    public boolean loadHomeScreenData() {
        System.out.println("Loading website data in to database...");

        try {
            InputStream inputStream = getClass()
                    .getClassLoader().getResourceAsStream(String.format("%s/%s", DATA_DIRECTORY, MAIN_SCREEN_DATA));

            if (inputStream == null) {
                System.out.println("Inputstream for website data is empty....");
                return false;
            }

            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            for (String line; (line = reader.readLine()) != null; ) {
                String[] separatedData = line.split("\\|");
                String type = separatedData[0];
                String filePath = separatedData[1];
                String title = separatedData.length > 2 ? separatedData[2] : null;
                String gender = separatedData.length > 3 ? separatedData[3] : null;

//                System.out.println(String.format("filePath = %s, title = %s",filePath, title));

                InputStream inputStream2 = getClass()
                        .getClassLoader().getResourceAsStream("static/images/" + filePath);

                if (inputStream2 == null) {
                    System.out.println("Unable to find path......" + filePath);
                    return false;
                }

                switch (type) {
                    case "brand":
                        BrandImages brandImages = new BrandImages(title, filePath);
                        ProductBrandCategory productBrandCategory = productBrandCategoryRepository.findByType(title);
                        if (productBrandCategory != null) {
                            brandImages.setProductBrandCategory(productBrandCategory);
                            brandImagesRepository.save(brandImages);
                        }
                        break;
                    case "category":
                        ApparelImages apparelImages = new ApparelImages(title, filePath);
                        ApparelCategory apparelCategory =
                                apparelCategoryRepository.findByType(title);

                        GenderCategory genderCategory = genderCategoryRepository.findByType(gender);
                        if (apparelCategory != null) {
                            apparelImages.setApparelCategory(apparelCategory);
                            apparelImages.setGenderCategory(genderCategory);
                            apparelImagesRepository.save(apparelImages);
                        }
                        break;
                    case "carousel":
                        CarouselImages carouselImages = new CarouselImages(title, filePath);
                        carouselImagesRepository.save(carouselImages);
                        break;
                    default:
                        System.out.println("Unsupported Type");
                }

            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean loadFixedPatternData(String filename, FileNameType filenameType) {
        System.out.println("Loading sortby data in to database...");

        try {
            InputStream inputStream = getClass()
                    .getClassLoader().getResourceAsStream(filename);

            if (inputStream == null) {
                System.out.println("Inputstream for website data is empty....");
                return false;
            }

            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);

            switch (filenameType) {
                case SORT_BY:
                    for (String line; (line = reader.readLine()) != null; ) {
                        System.out.println("SortBy Line = " + line);
                        SortByCategory sortByCategory = sortByCategoryRepository.findByType(line);
                        if (sortByCategory == null) {
                            sortByCategory = new SortByCategory(line);
                            sortByCategoryRepository.save(sortByCategory);
                        }
                    }
                    break;
                case PRICE_RANGE:
                    for (String line; (line = reader.readLine()) != null; ) {
                        System.out.println("PriceRange Line = " + line);
                        PriceRangeCategory priceRangeCategory = priceRangeCategoryRepository.findByType(line);
                        if (priceRangeCategory == null) {
                            priceRangeCategory = new PriceRangeCategory(line);
                            priceRangeCategoryRepository.save(priceRangeCategory);
                        }
                    }
                    break;
                default:
                    System.out.println("filename Type unsupported..");
                    reader.close();
                    return false;
            }
            reader.close();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private Optional<PriceRangeCategory> findPriceRangeCategory(int price) {

        if(price <= 50) {
            return priceRangeCategoryRepository.findById(1);
        } else if(price <= 100) {
            return priceRangeCategoryRepository.findById(2);
        } else if(price <= 200) {
            return priceRangeCategoryRepository.findById(3);
        } else if(price <= 300) {
            return priceRangeCategoryRepository.findById(4);
        } else if(price <= 400) {
            return priceRangeCategoryRepository.findById(5);
        } else {
            return priceRangeCategoryRepository.findById(6);
        }
    }

    public boolean loadTestData() {
        System.out.println("Loading test data in to database...");

        if (!loadFixedPatternData(String.format("%s/%s", DATA_DIRECTORY, SORT_BY_DATA), FileNameType.SORT_BY)) {
            return false;
        }

        if (!loadFixedPatternData(String.format("%s/%s", DATA_DIRECTORY, PRICE_RANGE_DATA), FileNameType.PRICE_RANGE)) {
            return false;
        }

        try {
            InputStream inputStream = getClass()
                    .getClassLoader().getResourceAsStream(String.format("%s/%s", DATA_DIRECTORY, WEB_DATA));

            if (inputStream == null) {
                System.out.println("Inputstream for test data is empty....");
                return false;
            }

            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            for (String line; (line = reader.readLine()) != null; ) {
                String[] result = line.split("\\|");
                String gender = result[1];
                String apparel = result[2];
                String brandName = result[3];
                String productName = result[4];
                String price = result[5];
                String fileName = result[6];
                String filePath = String.format("%s/%s/%s", removeSpaces(gender), removeSpaces(apparel), removeSpaces(fileName));

//                System.out.println("MainCategory = " + gender + ", SubCategory = " + subCategory
//                        + ", + BrandName = " + brandName + ", ProductName = " + productName + ", Price = "
//                        + price + ", filePath = " + filePath);

                InputStream inputStream2 = getClass()
                        .getClassLoader().getResourceAsStream("static/images/" + filePath);

                if (inputStream2 == null) {
                    System.out.println("Unable to find path......" + filePath);
                    return false;
                }

                GenderCategory genderCategory = genderCategoryRepository.findByType(gender);
                ApparelCategory apparelCategory = apparelCategoryRepository.findByType(apparel);
                ProductBrandCategory productBrandCategory = productBrandCategoryRepository.findByType(brandName);

                if (genderCategory == null) {
                    int genderId;

                    switch (gender.toLowerCase()) {
                        case "women":
                            genderId = WOMEN;
                            break;
                        case "men":
                            genderId = MEN;
                            break;
                        case "boys":
                            genderId = BOYS;
                            break;
                        case "girls":
                            genderId = GIRLS;
                            break;
                        case "home & living":
                            genderId = HOME_AND_LIVING;
                            break;
                        case "essentials":
                            genderId = ESSENTIALS;
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + gender.toLowerCase());
                    }
                    genderCategory = new GenderCategory(genderId, gender);
                    genderCategoryRepository.save(genderCategory);
                }

                if (apparelCategory == null) {
                    apparelCategory = new ApparelCategory(apparel);
                    apparelCategoryRepository.save(apparelCategory);
                }

                if (productBrandCategory == null) {
                    productBrandCategory = new ProductBrandCategory(brandName);
                    productBrandCategoryRepository.save(productBrandCategory);
                }

                Optional<PriceRangeCategory> priceRangeCategory = findPriceRangeCategory(Integer.parseInt(price));
                System.out.println("Starting Now..... ");
                System.out.println("Price == " + priceRangeCategory.get().getType());

                ProductInfo productInfo = new ProductInfo(1, productName, generateRandomDate(), productBrandCategory,
                        genderCategory, apparelCategory, priceRangeCategory.get(), Integer.parseInt(price),
                        generateRandomInt(1, 10), generateRandomInt(2, 5),
                        generateRandomFloat(0, 5, 1), true, filePath);

                productInfoRepository.save(productInfo);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!loadHomeScreenData()) {
            return false;
        }

        return true;
    }
}
