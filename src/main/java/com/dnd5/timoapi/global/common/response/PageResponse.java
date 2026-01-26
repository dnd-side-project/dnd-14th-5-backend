package com.dnd5.timoapi.global.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PageResponse<T> {

    private final List<T> content;
    private final long totalCount;
    private final int page;
    private final int size;
}
