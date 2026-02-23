// Type constants matching Prisma/backend enums
export const ItemType = {
  GEAR: 'GEAR',
  WEAPON: 'WEAPON',
  CONSUMABLE: 'CONSUMABLE',
  MATERIAL: 'MATERIAL',
  UNTRADABLE: 'UNTRADABLE',
  UNKNOWN: 'UNKNOWN',
  DEPRECATED: 'DEPRECATED',
} as const;
export type ItemType = (typeof ItemType)[keyof typeof ItemType];

export const Skill = {
  ATTACK: 'ATTACK',
  DEFENCE: 'DEFENCE',
  STRENGTH: 'STRENGTH',
  HITPOINTS: 'HITPOINTS',
  RANGED: 'RANGED',
  PRAYER: 'PRAYER',
  MAGIC: 'MAGIC',
  COOKING: 'COOKING',
  WOODCUTTING: 'WOODCUTTING',
  FLETCHING: 'FLETCHING',
  FISHING: 'FISHING',
  FIREMAKING: 'FIREMAKING',
  CRAFTING: 'CRAFTING',
  SMITHING: 'SMITHING',
  MINING: 'MINING',
  HERBLORE: 'HERBLORE',
  AGILITY: 'AGILITY',
  THIEVING: 'THIEVING',
  SLAYER: 'SLAYER',
  FARMING: 'FARMING',
  RUNECRAFT: 'RUNECRAFT',
  HUNTER: 'HUNTER',
  CONSTRUCTION: 'CONSTRUCTION',
} as const;
export type Skill = (typeof Skill)[keyof typeof Skill];

export const RequirementType = {
  SKILL: 'SKILL',
  ITEM: 'ITEM',
  QUEST: 'QUEST',
} as const;
export type RequirementType = (typeof RequirementType)[keyof typeof RequirementType];

// Item types
export interface Item {
  id: number;
  source_id: number;
  name: string;
  is_members: boolean;
  low_alch_price: number;
  high_alch_price: number;
  ge_buy_limit: number;
  examine_text: string;
  item_type: ItemType;
  track_price: boolean;
  created_at: string;
  updated_at: string;
}

export interface ItemCreate {
  id: number; // source_id alias
  name: string;
  members?: boolean;
  lowalch?: number;
  highalch?: number;
  limit?: number;
  examine?: string;
  item_type?: ItemType;
}

export interface ItemUpdate {
  name?: string;
  item_type?: ItemType;
  track_price?: boolean;
}

export interface ItemPrice {
  id: number;
  item_source_id: number;
  high_price: number;
  low_price: number;
  average_price: number;
  high_timestamp: string;
  low_timestamp: string;
  average_timestamp: string;
  created_at: string;
}

// Action requirement types (discriminated union)
export interface SkillRequirement {
  type: 'skill';
  skill: Skill;
  level: number;
}

export interface ItemRequirement {
  type: 'item';
  item_source_id: number;
  item_name: string;
  quantity: number;
}

export interface QuestRequirement {
  type: 'quest';
  quest_name: string;
  completed: boolean;
}

export type ActionRequirement = SkillRequirement | ItemRequirement | QuestRequirement;

// Database ActionRequirement (from Prisma)
export interface ActionRequirementDB {
  id: number;
  action_id: number;
  requirement_type: RequirementType;
  skill?: Skill;
  skill_level?: number;
  item_source_id?: number;
  item_quantity?: number;
  quest_name?: string;
  quest_completed?: boolean;
  order: number;
  item?: Item;
}

export interface ActionInput {
  id: number;
  action_id: number;
  item_source_id: number;
  quantity: number;
  item?: Item;
}

export interface ActionOutput {
  id: number;
  action_id: number;
  item_source_id: number;
  quantity: number;
  item?: Item;
}

// Action types
export interface Action {
  id: number;
  name: string;
  action_type: string;
  cost: number;
  cost_per_hour?: number;
  actions_per_hour: number;
  experience: number;
  experience_per_hour: number;
  requirements: ActionRequirementDB[];
  inputs: ActionInput[];
  outputs: ActionOutput[];
  created_at: string;
  updated_at: string;
}

export interface ActionCreate {
  name: string;
  action_type: string;
  actions_per_hour?: number;
  experience?: number;
  requirements?: ActionRequirement[];
  inputs?: { item_name: string; quantity: number }[];
  outputs?: { item_name: string; quantity: number }[];
}

// Task type enum (matches Prisma/Java TaskType)
export const TaskType = {
  GREEN_DRAGONS: 'GREEN_DRAGONS',
  HUMIDIFY: 'HUMIDIFY',
  TAN_LEATHER: 'TAN_LEATHER',
  WRATH_RUNECRAFT: 'WRATH_RUNECRAFT',
  VORKATH: 'VORKATH',
  RL_PVP: 'RL_PVP',
} as const;
export type TaskType = (typeof TaskType)[keyof typeof TaskType];

// Client types
export interface Client {
  id: number;
  instance_id: string;
  display_name: string | null;
  assigned_task: TaskType;
  task_params: Record<string, unknown> | null;
  first_seen_at: string;
  last_seen_at: string;
  is_online: boolean;
  created_at: string;
  updated_at: string;
}

export interface ClientUpdate {
  display_name?: string;
  assigned_task?: TaskType;
  task_params?: Record<string, unknown>;
}

export interface ClientStats {
  online_count: number;
  total_count: number;
  by_task: Record<string, number>;
}

export interface HealthCheckRecord {
  id: number;
  client_id: number;
  timestamp: number;
  task: string;
  hp: number;
  x: number;
  y: number;
  plane: number;
  in_combat: boolean;
  inventory_count: number;
  run_energy: number;
  xp_gained: Record<string, number>;
  created_at: string;
}

// Healthcheck types
export interface HealthCheckPayload {
  instanceId: string;
  timestamp: number;
  task: string;
  hp: number;
  x: number;
  y: number;
  plane: number;
  inCombat: boolean;
  inventoryCount: number;
  runEnergy: number;
  xpGained: Record<string, number>;
}

// Task types
export interface TaskResponse {
  task: string;
  params: Record<string, unknown>;
}

// API health response
export interface HealthResponse {
  status: string;
}
