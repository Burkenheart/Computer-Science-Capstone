//============================================================================
// Name        : ABCUProjectTwo.cpp
// Author      : Steven Valiant Burgett
// Version     : 2.0
// Description : ABCU Project Two - Binary Search Tree Implementation
//============================================================================

#include <iostream>     // for cout, cin
#include <fstream>      // for file input
#include <sstream>      // for stringstream parsing
#include <vector>       // for dynamic prerequisite storage
#include <cctype>       // for toupper()

using namespace std;

struct Course { // Course object to store course data
    string courseNumber;                 // Unique course ID (ex: CS300)
    string title;                        // Course title
    vector<string> prerequisites;        // List of prerequisite course numbers
};

struct Node { // BST Node structure
    Course course;       // Course data stored in node
    Node* left;          // Pointer to left subtree
    Node* right;         // Pointer to right subtree

    Node(Course aCourse) {     // Node constructor
        course = aCourse;      // Assign course data
        left = nullptr;        // Initialize left child
        right = nullptr;       // Initialize right child
    }
};

class BinarySearchTree { // Binary Search Tree class

private:
    Node* root;  // Root pointer of BST

    Node* insert(Node* node, Course course) { // Recursive insert helper

        if (node == nullptr) {                // Base case: empty spot found
            return new Node(course);          // Create new node
        }

        // Compare course numbers lexicographically
        if (course.courseNumber < node->course.courseNumber) {
            node->left = insert(node->left, course);   // Insert left
        }
        else {
            node->right = insert(node->right, course); // Insert right
        }

        return node;   // Return unchanged node pointer
    }

    void inOrder(Node* node) { // In-order traversal (sorted output)

        if (node == nullptr)
            return;  // Base case

        inOrder(node->left);   // Visit left subtree

        cout << node->course.courseNumber << ", "
            << node->course.title << endl;   // Print current node

        inOrder(node->right);  // Visit right subtree
    }

    Course* search(Node* node, string courseNumber) { // Recursive search helper

        if (node == nullptr)
            return nullptr;   // Course not found

        if (node->course.courseNumber == courseNumber)
            return &node->course;  // Return pointer to course if found

        if (courseNumber < node->course.courseNumber)
            return search(node->left, courseNumber);   // Search left subtree

        return search(node->right, courseNumber);      // Search right subtree
    }

public:
    BinarySearchTree() {
        root = nullptr;  // Initialize empty tree
    }

    void Insert(Course course) {
        root = insert(root, course);  // Call recursive insert
    }

    void PrintInOrder() {
        inOrder(root);   // Print courses in alphanumeric order
    }

    Course* Search(string courseNumber) {
        return search(root, courseNumber);   // Call recursive search
    }
};

string toUpper(string text) { // Convert string to uppercase for consistency

    for (int i = 0; i < text.length(); i++) {
        text[i] = toupper(text[i]);   // Convert each character
    }
    return text;  // Return modified string
}

void loadDataStructure(string fileName, BinarySearchTree& bst) { // Load CSV into BST

    ifstream file(fileName);   // Open input file stream

    if (!file.is_open()) {
        cout << "Error: Could not open file." << endl;  // File error handling
        return;
    }

    string line;

    while (getline(file, line)) {   // Read file line-by-line

        stringstream ss(line);      // Create string stream from line
        string token;

        Course course;             // Create course object for each row

        getline(ss, token, ',');    // Read course number
        course.courseNumber = token;

        getline(ss, token, ',');    // Read course title
        course.title = token;

        while (getline(ss, token, ',')) {  // Read prerequisites (if any)
            if (!token.empty()) {
                course.prerequisites.push_back(token);  // Store prerequisite
            }
        }

        bst.Insert(course);   // Insert fully built course into BST
    }

    file.close();   // Close file after reading

    cout << "Data loaded successfully." << endl;
}

void printCourse(BinarySearchTree& bst) { // Display specific course information

    string userInput;

    cout << "What course do you want to know about? ";
    cin >> userInput;    // Get course number from user

    userInput = toUpper(userInput);  // Normalize input for search

    Course* course = bst.Search(userInput);   // Search BST

    if (course == nullptr) {
        cout << "Course not found." << endl;  // Handle missing course
        return;
    }

    cout << endl;
    cout << course->courseNumber << ", "
        << course->title << endl;   // Display course information

    if (course->prerequisites.empty()) {
        cout << "Prerequisites: None" << endl;   // No prerequisites case
    }
    else {
        cout << "Prerequisites: ";
        for (size_t i = 0; i < course->prerequisites.size(); i++) {
            cout << course->prerequisites[i];    // Print each prerequisite
            if (i < course->prerequisites.size() - 1)
                cout << ", ";   // Add comma between prerequisites
        }
        cout << endl;
    }

    cout << endl;
}

int main() {

    BinarySearchTree bst;   // Instantiate BST object

    int choice = 0;         // Menu selection variable
    string fileName;        // Stores user-entered file name

    cout << "Welcome to the course planner." << endl << endl;

    while (choice != 9) {   // Loop until user exits

        cout << "1. Load Data Structure." << endl;
        cout << "2. Print Course List." << endl;
        cout << "3. Print Course." << endl;
        cout << "9. Exit" << endl << endl;

        cout << "What would you like to do? ";
        cin >> choice;      // Get numeric menu choice

        cout << endl;

        switch (choice) {

        case 1:
            cout << "Enter file name: ";
            cin >> fileName;   // Get CSV file name
            loadDataStructure(fileName, bst);   // Load data
            cout << endl;
            break;

        case 2:
            bst.PrintInOrder();  // Print all courses in sorted order
            cout << endl;
            break;

        case 3:
            printCourse(bst);    // Print specific course
            break;

        case 9:
            cout << "Thank you for using the course planner!" << endl;
            break;

        default:
            cout << choice << " is not a valid option." << endl << endl;  // Invalid input handling
        }
    }

    return 0;   // Program ends successfully
}