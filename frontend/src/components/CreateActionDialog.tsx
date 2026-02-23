import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { createAction, Skill, type ActionRequirement } from '@/api';

interface RequirementInput {
  type: 'skill' | 'item' | 'quest';
  skill?: string;
  level?: string;
  item_source_id?: string;
  item_name?: string;
  quantity?: string;
  quest_name?: string;
}

interface ItemIO {
  item_name: string;
  quantity: string;
}

export function CreateActionDialog() {
  const [open, setOpen] = useState(false);
  const queryClient = useQueryClient();

  const [formData, setFormData] = useState({
    name: '',
    action_type: '',
    actions_per_hour: '',
    experience: '',
  });

  const [requirements, setRequirements] = useState<RequirementInput[]>([]);
  const [inputs, setInputs] = useState<ItemIO[]>([]);
  const [outputs, setOutputs] = useState<ItemIO[]>([]);

  const mutation = useMutation({
    mutationFn: createAction,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['actions'] });
      queryClient.invalidateQueries({ queryKey: ['actions-count'] });
      setOpen(false);
      resetForm();
    },
  });

  const resetForm = () => {
    setFormData({
      name: '',
      action_type: '',
      actions_per_hour: '',
      experience: '',
    });
    setRequirements([]);
    setInputs([]);
    setOutputs([]);
  };

  const addRequirement = () => {
    setRequirements([...requirements, { type: 'skill', skill: 'MAGIC', level: '1' }]);
  };

  const updateRequirement = (index: number, field: string, value: string) => {
    const updated = [...requirements];
    updated[index] = { ...updated[index], [field]: value };
    setRequirements(updated);
  };

  const removeRequirement = (index: number) => {
    setRequirements(requirements.filter((_, i) => i !== index));
  };

  const addInput = () => {
    setInputs([...inputs, { item_name: '', quantity: '1' }]);
  };

  const updateInput = (index: number, field: string, value: string) => {
    const updated = [...inputs];
    updated[index] = { ...updated[index], [field]: value };
    setInputs(updated);
  };

  const removeInput = (index: number) => {
    setInputs(inputs.filter((_, i) => i !== index));
  };

  const addOutput = () => {
    setOutputs([...outputs, { item_name: '', quantity: '1' }]);
  };

  const updateOutput = (index: number, field: string, value: string) => {
    const updated = [...outputs];
    updated[index] = { ...updated[index], [field]: value };
    setOutputs(updated);
  };

  const removeOutput = (index: number) => {
    setOutputs(outputs.filter((_, i) => i !== index));
  };

  const buildRequirements = (): ActionRequirement[] => {
    return requirements.map((req) => {
      if (req.type === 'skill') {
        return {
          type: 'skill' as const,
          skill: req.skill as keyof typeof Skill,
          level: parseInt(req.level || '1'),
        };
      } else if (req.type === 'item') {
        return {
          type: 'item' as const,
          item_source_id: parseInt(req.item_source_id || '0'),
          item_name: req.item_name || '',
          quantity: parseInt(req.quantity || '1'),
        };
      } else {
        return {
          type: 'quest' as const,
          quest_name: req.quest_name || '',
          completed: true,
        };
      }
    });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    mutation.mutate({
      name: formData.name,
      action_type: formData.action_type,
      actions_per_hour: formData.actions_per_hour ? parseInt(formData.actions_per_hour) : undefined,
      experience: formData.experience ? parseInt(formData.experience) : undefined,
      requirements: buildRequirements(),
      inputs: inputs
        .filter((i) => i.item_name)
        .map((i) => ({
          item_name: i.item_name,
          quantity: parseInt(i.quantity || '1'),
        })),
      outputs: outputs
        .filter((o) => o.item_name)
        .map((o) => ({
          item_name: o.item_name,
          quantity: parseInt(o.quantity || '1'),
        })),
    });
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>Create Action</Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[600px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Create New Action</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="name">Name *</Label>
              <Input
                id="name"
                required
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="e.g. Plank Make"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="action_type">Action Type *</Label>
              <Input
                id="action_type"
                required
                value={formData.action_type}
                onChange={(e) => setFormData({ ...formData, action_type: e.target.value })}
                placeholder="e.g. spell, activity"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="actions_per_hour">Actions/Hour</Label>
              <Input
                id="actions_per_hour"
                type="number"
                value={formData.actions_per_hour}
                onChange={(e) => setFormData({ ...formData, actions_per_hour: e.target.value })}
                placeholder="1"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="experience">Experience</Label>
              <Input
                id="experience"
                type="number"
                value={formData.experience}
                onChange={(e) => setFormData({ ...formData, experience: e.target.value })}
                placeholder="0"
              />
            </div>
          </div>

          <Tabs defaultValue="requirements" className="w-full">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="requirements">Requirements ({requirements.length})</TabsTrigger>
              <TabsTrigger value="inputs">Inputs ({inputs.length})</TabsTrigger>
              <TabsTrigger value="outputs">Outputs ({outputs.length})</TabsTrigger>
            </TabsList>

            <TabsContent value="requirements" className="space-y-2">
              {requirements.map((req, index) => (
                <div key={index} className="flex gap-2 items-end p-2 border rounded">
                  <div className="space-y-1">
                    <Label className="text-xs">Type</Label>
                    <Select
                      value={req.type}
                      onValueChange={(value) => updateRequirement(index, 'type', value)}
                    >
                      <SelectTrigger className="w-24">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="skill">Skill</SelectItem>
                        <SelectItem value="item">Item</SelectItem>
                        <SelectItem value="quest">Quest</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  {req.type === 'skill' && (
                    <>
                      <div className="space-y-1 flex-1">
                        <Label className="text-xs">Skill</Label>
                        <Select
                          value={req.skill}
                          onValueChange={(value) => updateRequirement(index, 'skill', value)}
                        >
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            {Object.keys(Skill).map((skill) => (
                              <SelectItem key={skill} value={skill}>
                                {skill}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-1 w-20">
                        <Label className="text-xs">Level</Label>
                        <Input
                          type="number"
                          min="1"
                          max="99"
                          value={req.level}
                          onChange={(e) => updateRequirement(index, 'level', e.target.value)}
                        />
                      </div>
                    </>
                  )}

                  {req.type === 'item' && (
                    <>
                      <div className="space-y-1 w-24">
                        <Label className="text-xs">Item ID</Label>
                        <Input
                          type="number"
                          value={req.item_source_id}
                          onChange={(e) => updateRequirement(index, 'item_source_id', e.target.value)}
                        />
                      </div>
                      <div className="space-y-1 flex-1">
                        <Label className="text-xs">Name</Label>
                        <Input
                          value={req.item_name}
                          onChange={(e) => updateRequirement(index, 'item_name', e.target.value)}
                        />
                      </div>
                      <div className="space-y-1 w-20">
                        <Label className="text-xs">Qty</Label>
                        <Input
                          type="number"
                          min="1"
                          value={req.quantity}
                          onChange={(e) => updateRequirement(index, 'quantity', e.target.value)}
                        />
                      </div>
                    </>
                  )}

                  {req.type === 'quest' && (
                    <div className="space-y-1 flex-1">
                      <Label className="text-xs">Quest Name</Label>
                      <Input
                        value={req.quest_name}
                        onChange={(e) => updateRequirement(index, 'quest_name', e.target.value)}
                        placeholder="e.g. Lunar Diplomacy"
                      />
                    </div>
                  )}

                  <Button
                    type="button"
                    variant="destructive"
                    size="sm"
                    onClick={() => removeRequirement(index)}
                  >
                    X
                  </Button>
                </div>
              ))}
              <Button type="button" variant="outline" size="sm" onClick={addRequirement}>
                + Add Requirement
              </Button>
            </TabsContent>

            <TabsContent value="inputs" className="space-y-2">
              {inputs.map((input, index) => (
                <div key={index} className="flex gap-2 items-end p-2 border rounded">
                  <div className="space-y-1 flex-1">
                    <Label className="text-xs">Item Name</Label>
                    <Input
                      value={input.item_name}
                      onChange={(e) => updateInput(index, 'item_name', e.target.value)}
                      placeholder="e.g. Nature rune"
                    />
                  </div>
                  <div className="space-y-1 w-24">
                    <Label className="text-xs">Quantity</Label>
                    <Input
                      type="number"
                      min="1"
                      value={input.quantity}
                      onChange={(e) => updateInput(index, 'quantity', e.target.value)}
                    />
                  </div>
                  <Button
                    type="button"
                    variant="destructive"
                    size="sm"
                    onClick={() => removeInput(index)}
                  >
                    X
                  </Button>
                </div>
              ))}
              <Button type="button" variant="outline" size="sm" onClick={addInput}>
                + Add Input
              </Button>
            </TabsContent>

            <TabsContent value="outputs" className="space-y-2">
              {outputs.map((output, index) => (
                <div key={index} className="flex gap-2 items-end p-2 border rounded">
                  <div className="space-y-1 flex-1">
                    <Label className="text-xs">Item Name</Label>
                    <Input
                      value={output.item_name}
                      onChange={(e) => updateOutput(index, 'item_name', e.target.value)}
                      placeholder="e.g. Oak plank"
                    />
                  </div>
                  <div className="space-y-1 w-24">
                    <Label className="text-xs">Quantity</Label>
                    <Input
                      type="number"
                      min="1"
                      value={output.quantity}
                      onChange={(e) => updateOutput(index, 'quantity', e.target.value)}
                    />
                  </div>
                  <Button
                    type="button"
                    variant="destructive"
                    size="sm"
                    onClick={() => removeOutput(index)}
                  >
                    X
                  </Button>
                </div>
              ))}
              <Button type="button" variant="outline" size="sm" onClick={addOutput}>
                + Add Output
              </Button>
            </TabsContent>
          </Tabs>

          <div className="flex justify-end gap-2 pt-4">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? 'Creating...' : 'Create Action'}
            </Button>
          </div>

          {mutation.isError && (
            <p className="text-sm text-destructive">
              Error: {(mutation.error as Error).message}
            </p>
          )}
        </form>
      </DialogContent>
    </Dialog>
  );
}
