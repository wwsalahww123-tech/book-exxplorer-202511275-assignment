

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Scanner;

public class App {

    static final String BASE_URL = "https://books.toscrape.com/";
    static final int MAX_BOOKS = 20;

    static class Book {
        String title;
        String price;
        String url;

        Book(String title, String price, String url) {
            this.title = title;
            this.price = price;
            this.url = url;
        }
    }

    static Book[] catalog = new Book[MAX_BOOKS];
    static int catalogCount = 0;
    static Book[] readingList = new Book[MAX_BOOKS];
    static int readingCount = 0;

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int choice;
        do {
            printMenu();
            choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1:
                    exploreBooks();
                    break;
                case 2:
                    searchBook(readText("Enter title: "));
                    break;
                case 3:
                    searchBook(readText("Enter author/price: "), true);
                    break;
                case 4:
                    addBook();
                    break;
                case 5:
                    viewReadingList();
                    break;
                case 6:
                    removeBook();
                    break;
                case 7:
                    displayBookDetails();
                    break;
                case 0:
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice, try again.");
            }
            System.out.println();
        } while (choice != 0);
    }

    static void printMenu() {
        System.out.println("===== GUTENBERG BOOK EXPLORER =====");
        System.out.println("1. Explore Books");
        System.out.println("2. Search by Title");
        System.out.println("3. Search by Author/Price");
        System.out.println("4. Add Book to Reading List");
        System.out.println("5. View Reading List");
        System.out.println("6. Remove Book");
        System.out.println("7. Display Book Details");
        System.out.println("0. Exit");
    }

    static int readInt(String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    static boolean loadCatalog() {
        try {
            Document doc = Jsoup.connect(BASE_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            Elements items = doc.select("article.product_pod");
            if (items.isEmpty()) {
                System.out.println("No books found on the page.");
                return false;
            }
            catalogCount = 0;
            for (Element item : items) {
                if (catalogCount == MAX_BOOKS) {
                    break;
                }
                Element link = item.selectFirst("h3 a");
                if (link == null) {
                    continue;
                }
                String title = link.attr("title");
                String url = link.absUrl("href");
                String price = item.select("p.price_color").text().replaceAll("[^0-9.]", "");
                catalog[catalogCount] = new Book(title, price, url);
                catalogCount++;
            }
            return catalogCount > 0;
        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());
            return false;
        }
    }

    static boolean ensureCatalog() {
        if (catalogCount > 0) {
            return true;
        }
        return loadCatalog();
    }

    static void printBooks(Book[] list, int count) {
        for (int i = 0; i < count; i++) {
            System.out.println((i + 1) + ". " + list[i].title + " - " + list[i].price + " GBP");
        }
    }

    static void exploreBooks() {
        System.out.println("Loading books...");
        if (!loadCatalog()) {
            return;
        }
        printBooks(catalog, catalogCount);
    }

    static void searchBook(String title) {
        searchBook(title, false);
    }

    static void searchBook(String keyword, boolean searchByAuthor) {
        if (keyword.isEmpty()) {
            System.out.println("Search text cannot be empty.");
            return;
        }
        if (!ensureCatalog()) {
            return;
        }
        String k = keyword.toLowerCase();
        int found = 0;
        for (int i = 0; i < catalogCount; i++) {
            String field = searchByAuthor ? catalog[i].price : catalog[i].title;
            if (field.toLowerCase().contains(k)) {
                System.out.println((i + 1) + ". " + catalog[i].title + " - " + catalog[i].price + " GBP");
                found++;
            }
        }
        if (found == 0) {
            System.out.println("No books found.");
        }
    }

    static void addBook() {
        if (readingCount == MAX_BOOKS) {
            System.out.println("Reading list is full!");
            return;
        }
        if (!ensureCatalog()) {
            return;
        }
        printBooks(catalog, catalogCount);
        int n = readInt("Enter book number to add (1-" + catalogCount + "): ");
        if (n < 1 || n > catalogCount) {
            System.out.println("Invalid book number.");
            return;
        }
        Book book = catalog[n - 1];
        for (int i = 0; i < readingCount; i++) {
            if (readingList[i].url.equals(book.url)) {
                System.out.println("This book is already in your reading list.");
                return;
            }
        }
        readingList[readingCount] = book;
        readingCount++;
        System.out.println("Book added to your reading list.");
    }

    static void viewReadingList() {
        if (readingCount == 0) {
            System.out.println("Your reading list is empty.");
            return;
        }
        System.out.println("--- Your Reading List ---");
        for (int i = 0; i < readingCount; i++) {
            System.out.println((i + 1) + ". " + readingList[i].title + " - " + readingList[i].price + " GBP");
        }
    }

    static void removeBook() {
        if (readingCount == 0) {
            System.out.println("Your reading list is empty.");
            return;
        }
        viewReadingList();
        int n = readInt("Enter book number to remove (1-" + readingCount + "): ");
        if (n < 1 || n > readingCount) {
            System.out.println("Invalid book number.");
            return;
        }
        for (int i = n - 1; i < readingCount - 1; i++) {
            readingList[i] = readingList[i + 1];
        }
        readingCount--;
        readingList[readingCount] = null;
        System.out.println("Book removed.");
    }

    static void displayBookDetails() {
        if (!ensureCatalog()) {
            return;
        }
        printBooks(catalog, catalogCount);
        int n = readInt("Enter book number (1-" + catalogCount + "): ");
        if (n < 1 || n > catalogCount) {
            System.out.println("Invalid book number.");
            return;
        }
        Book b = catalog[n - 1];
        System.out.println("--- Book Details ---");
        System.out.println("Title: " + b.title);
        System.out.println("Author/Price: " + b.price + " GBP");
        System.out.println("URL: " + b.url);
    }
}