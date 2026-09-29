import React, { useState } from 'react';
import {
  StyleSheet,
  Alert,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Input } from '../common/Input';
import { FormModal } from '../common/FormModal';
import { salesApi } from '../../api/salesApi';

interface ConvertSaleModalProps {
  visible: boolean;
  leadId: number;
  leadName: string;
  onClose: () => void;
  onConverted: () => void;
}

export const ConvertSaleModal: React.FC<ConvertSaleModalProps> = ({
  visible,
  leadId,
  leadName,
  onClose,
  onConverted,
}) => {
  const [dealValue, setDealValue] = useState('');
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleConvert = async () => {
    const val = parseFloat(dealValue);
    if (isNaN(val) || val <= 0) {
      Alert.alert('Validation Error', 'Please enter a valid deal value.');
      return;
    }

    setSubmitting(true);
    try {
      await salesApi.convertLead({
        leadId,
        dealValue: val,
        notes: notes.trim() || undefined,
      });

      Alert.alert('Congratulations! 🎉', `Lead ${leadName} successfully converted to a Sale!`);
      onConverted();
      onClose();
    } catch (err: any) {
      Alert.alert('Error', err.message || 'Failed to convert lead.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <FormModal
      visible={visible}
      onClose={onClose}
      title="Convert to Sale"
      subtitle={leadName}
      onSave={handleConvert}
      saveTitle="Save"
      saveVariant="success"
      saveLoading={submitting}
      saveIcon={<Ionicons name="checkmark-circle" size={18} color="#ffffff" />}
      heightPercent={0.78}
      maxHeightPixels={520}
    >
      <Input
        label="Deal Value (₹) *"
        placeholder="e.g. 25000"
        value={dealValue}
        onChangeText={setDealValue}
        keyboardType="numeric"
        leftIcon="cash-outline"
      />

      <Input
        label="Sale Notes & Plan Details"
        placeholder="Payment method, plan term, etc..."
        value={notes}
        onChangeText={setNotes}
        multiline
        numberOfLines={3}
        style={styles.textArea}
      />
    </FormModal>
  );
};

const styles = StyleSheet.create({
  textArea: {
    height: 80,
    textAlignVertical: 'top',
  },
});
