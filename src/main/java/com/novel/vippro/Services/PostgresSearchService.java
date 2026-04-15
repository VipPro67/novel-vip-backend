package com.novel.vippro.Services;

import com.novel.vippro.DTO.Novel.NovelSearchDTO;
import com.novel.vippro.DTO.Novel.SearchSuggestion;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Repository.NovelRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostgresSearchService implements SearchService {

    private final NovelRepository novelRepository;

    @Override
    public void indexNovels(List<Novel> novels) {
        // For PostgreSQL, indexing is handled by the database itself via B-Tree or GIN indexes.
        // No external indexing is required.
    }

    @Override
    public void deleteNovel(UUID id) {
        // Data is already removed from DB, no external search index to clean up.
    }

    @Override
    public Page<Novel> search(NovelSearchDTO searchDTO, Pageable pageable) {
        return novelRepository.searchByCriteria(
                searchDTO.keyword(),
                searchDTO.title(),
                searchDTO.author(),
                searchDTO.category(),
                searchDTO.genre(),
                searchDTO.tag(),
                pageable
        );
    }

    @Override
    public List<SearchSuggestion> suggest(String query, int limit) {
        return novelRepository.findByTitleContainingIgnoreCaseOrderByRatingDesc(query, Pageable.ofSize(limit))
                .getContent()
                .stream()
                .map(novel -> new SearchSuggestion(novel.getId().toString(), novel.getTitle()))
                .collect(Collectors.toList());
    }
}
