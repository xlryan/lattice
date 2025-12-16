import { SearchResult } from '../types';

const RAW_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';
const API_BASE_URL = RAW_BASE_URL.endsWith('/') ? RAW_BASE_URL.slice(0, -1) : RAW_BASE_URL;

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
  const endpoint = `${API_BASE_URL}/api/search`;
  const response = await fetch(endpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ query, domain, filter: null, topK, minSimilarity: 0.3 })
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`Search request failed: ${response.status} ${text}`);
  }

  const payload = await response.json() as SearchResponsePayload;
  const mapped: SearchResult[] = payload.results.map(item => ({
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
