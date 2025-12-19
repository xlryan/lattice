import request from '../requestConfig';
import { SearchResult } from '../types';

interface SearchResponsePayload {
  results: Array<{
    id: string;
    title: string;
    snippet: string;
    score: number;
  }>;
  tookMillis: number;
}

export async function hybridSearch(query: string, domain: string, topK = 8): Promise<{ results: SearchResult[]; took: number; }> {
  const payload = await request<SearchResponsePayload>('/search', {
    method: 'POST',
    data: { query, domain, filter: null, topK, minSimilarity: 0.3 }
  });

  const mapped: SearchResult[] = (payload.results || []).map(item => ({
    id: item.id,
    title: item.title,
    snippet: item.snippet,
    score: item.score,
    source: domain,
    tags: [],
    date: new Date().toISOString().split('T')[0]
  }));

  return { results: mapped, took: payload.tookMillis };
}
