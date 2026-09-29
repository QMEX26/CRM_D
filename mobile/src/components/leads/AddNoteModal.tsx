import React, { useState } from 'react';
import {
  StyleSheet,
  Alert,
} from 'react-native';
import { Input } from '../common/Input';
import { FormModal } from '../common/FormModal';
import { noteApi } from '../../api/noteApi';

interface AddNoteModalProps {
  visible: boolean;
  leadId: number;
  leadName: string;
  onClose: () => void;
  onNoteAdded: () => void;
}

export const AddNoteModal: React.FC<AddNoteModalProps> = ({
  visible,
  leadId,
  leadName,
  onClose,
  onNoteAdded,
}) => {
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSave = async () => {
    if (!content.trim()) {
      Alert.alert('Validation Error', 'Note content cannot be empty.');
      return;
    }

    setSubmitting(true);
    try {
      await noteApi.addNote(leadId, content.trim());
      setContent('');
      Alert.alert('Success', 'Note added successfully.');
      onNoteAdded();
      onClose();
    } catch (err: any) {
      Alert.alert('Error', err.message || 'Failed to add note.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <FormModal
      visible={visible}
      onClose={onClose}
      title="Add Lead Note"
      subtitle={leadName}
      onSave={handleSave}
      saveTitle="Save"
      saveLoading={submitting}
      saveVariant="primary"
      heightPercent={0.70}
      maxHeightPixels={480}
    >
      <Input
        placeholder="Write internal note, client preference, or background info..."
        value={content}
        onChangeText={setContent}
        multiline
        numberOfLines={4}
      />
    </FormModal>
  );
};
