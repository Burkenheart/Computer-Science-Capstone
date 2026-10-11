from pymongo import MongoClient
from pymongo.errors import PyMongoError


class AnimalShelter:
    """CRUD operations for the AAC animal shelter MongoDB collection."""

    def __init__(self, username, password):
        """Initialize the MongoDB connection."""
        self.client = MongoClient(
            f"mongodb://{username}:{password}@localhost:27017/aac?authSource=admin"
        )
        self.database = self.client["aac"]
        self.collection = self.database["animals"]

    def create(self, data):
        """
        Insert a document into the animals collection.

        :param data: Dictionary containing animal data
        :return: True if insert is successful, otherwise False
        """
        if data is not None and isinstance(data, dict):
            try:
                result = self.collection.insert_one(data)
                return result.acknowledged
            except PyMongoError as error:
                print(f"Create operation failed: {error}")
                return False
        return False

    def read(self, query):
        """
        Query documents from the animals collection.

        :param query: Dictionary containing search criteria
        :return: List of matching documents, or empty list if unsuccessful
        """
        if query is not None and isinstance(query, dict):
            try:
                results = self.collection.find(query)
                return list(results)
            except PyMongoError as error:
                print(f"Read operation failed: {error}")
                return []
        return []

    def update(self, query, new_values):
        """
        Update documents in the animals collection.

        :param query: Dictionary containing search criteria
        :param new_values: Dictionary containing updated values
        :return: Number of modified documents
        """
        if query is not None and new_values is not None:
            try:
                result = self.collection.update_many(
                    query,
                    {"$set": new_values}
                )
                return result.modified_count
            except PyMongoError as error:
                print(f"Update operation failed: {error}")
                return 0
        return 0

    def delete(self, query):
        """
        Delete documents from the animals collection.

        :param query: Dictionary containing search criteria
        :return: Number of deleted documents
        """
        if query is not None and isinstance(query, dict):
            try:
                result = self.collection.delete_many(query)
                return result.deleted_count
            except PyMongoError as error:
                print(f"Delete operation failed: {error}")
                return 0
        return 0