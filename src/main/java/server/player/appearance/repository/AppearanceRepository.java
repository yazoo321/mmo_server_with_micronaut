package server.player.appearance.repository;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import server.configuration.PlayerCharacterConfiguration;
import server.player.appearance.model.AppearancePiece;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class AppearanceRepository {

    // This repository is connected to MongoDB
    PlayerCharacterConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<AppearancePiece> appearancePieceCollection;

    public AppearanceRepository(
            PlayerCharacterConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    private void savePieces(List<AppearancePiece> pieceList) {

    }

    private void prepareCollections() {
        this.appearancePieceCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), AppearancePiece.class);
    }
}
