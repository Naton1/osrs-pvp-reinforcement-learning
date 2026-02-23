import { apiClient } from './client';
import type {
  Action,
  ActionCreate,
  ActionRequirementDB,
  ActionInput,
  ActionOutput,
} from './types';

export async function getActions(limit = 100): Promise<Action[]> {
  const response = await apiClient.get<Action[]>('/actions/', { params: { limit } });
  return response.data;
}

export async function getAction(id: number): Promise<Action> {
  const response = await apiClient.get<Action>(`/actions/${id}`);
  return response.data;
}

export async function getActionByName(name: string): Promise<Action> {
  const response = await apiClient.get<Action>(`/actions/name/${encodeURIComponent(name)}`);
  return response.data;
}

export async function createAction(action: ActionCreate): Promise<Action> {
  const response = await apiClient.post<Action>('/actions/', action);
  return response.data;
}

export async function getActionRequirements(actionId: number): Promise<ActionRequirementDB[]> {
  const response = await apiClient.get<ActionRequirementDB[]>(
    `/actions/${actionId}/requirements`
  );
  return response.data;
}

export async function addActionRequirement(
  actionId: number,
  requirement: {
    type: string;
    skill?: string;
    level?: number;
    item_source_id?: number;
    item_name?: string;
    quantity?: number;
    quest_name?: string;
    completed?: boolean;
  }
): Promise<ActionRequirementDB> {
  const response = await apiClient.post<ActionRequirementDB>(
    `/actions/${actionId}/requirements`,
    requirement
  );
  return response.data;
}

export async function getActionInputs(actionId: number): Promise<ActionInput[]> {
  const response = await apiClient.get<ActionInput[]>(`/actions/${actionId}/inputs`);
  return response.data;
}

export async function addActionInput(
  actionId: number,
  input: { item_name: string; quantity: number }
): Promise<ActionInput> {
  const response = await apiClient.post<ActionInput>(`/actions/${actionId}/inputs`, input);
  return response.data;
}

export async function addActionOutput(
  actionId: number,
  output: { item_name: string; quantity: number }
): Promise<ActionOutput> {
  const response = await apiClient.post<ActionOutput>(`/actions/${actionId}/outputs`, output);
  return response.data;
}
